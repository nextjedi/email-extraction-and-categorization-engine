# Data Privacy & Multi-Tenancy Architecture

## Overview

This document outlines the comprehensive data privacy strategy for the Intelligent Message Processor when handling multiple users.

## Privacy Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│ Layer 1: Authentication & Authorization (OAuth2/JWT)│
├─────────────────────────────────────────────────────┤
│ Layer 2: API-Level User Isolation                   │
├─────────────────────────────────────────────────────┤
│ Layer 3: Database Row-Level Security (RLS)          │
├─────────────────────────────────────────────────────┤
│ Layer 4: Kafka Message Encryption & Partitioning    │
├─────────────────────────────────────────────────────┤
│ Layer 5: Data Encryption (At Rest & In Transit)     │
├─────────────────────────────────────────────────────┤
│ Layer 6: Redis Key Isolation                        │
├─────────────────────────────────────────────────────┤
│ Layer 7: Audit Logging & Compliance                 │
└─────────────────────────────────────────────────────┘
```

## Multi-Tenancy Strategies

### Strategy 1: Shared Database with Row-Level Security (RECOMMENDED)
**Current Implementation - Enhanced**

✅ **Pros:**
- Cost-effective for small to medium deployments
- Easier to manage and maintain
- Good performance with proper indexing
- Simpler backup and disaster recovery

⚠️ **Cons:**
- Requires careful security implementation
- Potential for SQL injection if not careful
- All users on same database instance

**Implementation:**
- All users share the same database
- Every table has a `user_id` column
- PostgreSQL Row-Level Security (RLS) enforced
- Application-level filtering as additional layer
- Kafka message keys include user_id

### Strategy 2: Database Per Tenant
**For High-Security Requirements**

✅ **Pros:**
- Complete data isolation
- Easier compliance (GDPR, HIPAA)
- Can encrypt individual databases
- Better for large enterprises

⚠️ **Cons:**
- Higher operational complexity
- More expensive
- Harder to manage at scale
- Cross-tenant analytics difficult

### Strategy 3: Schema Per Tenant
**Middle Ground Approach**

✅ **Pros:**
- Good isolation within shared database
- Easier than separate databases
- Logical separation

⚠️ **Cons:**
- Still shares same DB instance
- Schema sprawl with many tenants

## Recommended Implementation (Strategy 1 Enhanced)

### 1. Authentication & Authorization

**JWT-Based Authentication:**
```
User Login → OAuth2 Provider → JWT Token
                                    ↓
                        Token contains: {
                          userId: "user123",
                          email: "user@example.com",
                          roles: ["USER"],
                          tenant: "user123"
                        }
                                    ↓
                        Every API call includes token
                                    ↓
                        Services validate & extract userId
```

### 2. Database Row-Level Security

**PostgreSQL RLS Implementation:**

```sql
-- Enable RLS on all tables
ALTER TABLE extracted_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE classified_messages ENABLE ROW LEVEL SECURITY;

-- Create policy: Users can only see their own data
CREATE POLICY user_isolation_policy ON extracted_messages
    USING (user_id = current_setting('app.current_user_id')::text);

CREATE POLICY user_isolation_policy ON classified_messages
    USING (user_id = current_setting('app.current_user_id')::text);

-- Prevent users from seeing other users' data even with SQL injection
CREATE POLICY prevent_other_users ON extracted_messages
    FOR ALL
    USING (user_id = current_setting('app.current_user_id')::text)
    WITH CHECK (user_id = current_setting('app.current_user_id')::text);
```

### 3. Application-Level Enforcement

**Always filter by userId in queries:**

```java
// BAD - No user filtering
List<Message> messages = repository.findAll();

// GOOD - Always filter by authenticated user
List<Message> messages = repository.findByUserId(authenticatedUserId);
```

### 4. Kafka Message Isolation

**Partitioning Strategy:**

```
Topic: raw-messages.extracted
Partitions: 10

Message Key: userId
├─ user123 → Partition 3 (consistent hashing)
├─ user456 → Partition 7
└─ user789 → Partition 1

Consumer Group Filtering:
- Each message has userId in payload
- Consumers validate userId before processing
- Messages encrypted with user-specific keys
```

### 5. Redis Key Namespacing

```
# Bad - No isolation
key: "message:12345"

# Good - User-specific namespacing
key: "user:user123:message:12345"
key: "user:user456:classification:67890"
```

### 6. Encryption Strategy

**Data at Rest:**
```
PostgreSQL:
- Transparent Data Encryption (TDE) on disk
- Column-level encryption for sensitive fields (email body)
- Per-user encryption keys (managed by KMS)

Redis:
- Encrypted snapshots
- TLS for replication
```

**Data in Transit:**
```
- HTTPS/TLS 1.3 for all API calls
- Kafka SSL/TLS encryption
- Database SSL connections
- Redis TLS
```

### 7. API Security

**Request Validation Flow:**

```
1. API Request arrives
2. Extract JWT token from Authorization header
3. Validate token signature and expiry
4. Extract userId from token
5. Validate userId matches request parameter
6. Set userId in thread context
7. Execute query with userId filter
8. Return only user's data
```

## Privacy Guarantees

### For User A (user123):

✅ **Can Access:**
- Their own messages in `extracted_messages` where `user_id = 'user123'`
- Their own classifications in `classified_messages` where `user_id = 'user123'`
- Their own Redis cache entries under `user:user123:*`
- Their own Kafka messages (filtered by userId)

❌ **Cannot Access:**
- User B's messages (database RLS blocks)
- User C's classifications (application filters)
- Other users' Redis keys (namespace isolation)
- Other users' Kafka messages (encryption + filtering)

### Multi-Layer Defense

**Layer 1: API Gateway (Future)**
- Rate limiting per user
- IP whitelisting
- WAF protection

**Layer 2: Application Code**
```java
// Security interceptor validates every request
@GetMapping("/users/{userId}/messages")
public List<Message> getMessages(
    @PathVariable String userId,
    @AuthenticationPrincipal UserDetails user
) {
    // Validate user can only access their own data
    if (!userId.equals(user.getUsername())) {
        throw new ForbiddenException("Access denied");
    }

    // Additional check at service layer
    return service.getMessages(userId);
}
```

**Layer 3: Database**
- RLS policies prevent SQL injection attacks
- Even if attacker bypasses app layer, DB blocks access

**Layer 4: Encryption**
- Even if database is compromised, data is encrypted

## Compliance & Audit

### GDPR Compliance

**Right to Access:**
```bash
GET /api/v1/users/{userId}/data-export
→ Returns all user's data in JSON format
```

**Right to Deletion:**
```bash
DELETE /api/v1/users/{userId}/data
→ Deletes all messages, classifications, cache entries
→ Publishes tombstone records to Kafka
→ Logs deletion in audit trail
```

**Data Portability:**
```bash
GET /api/v1/users/{userId}/export?format=json
→ Exports all data in standard format
```

### Audit Logging

**Every sensitive operation logged:**

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "userId": "user123",
  "action": "MESSAGE_ACCESSED",
  "resource": "message:12345",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "result": "SUCCESS"
}
```

**Logged Actions:**
- Message extraction
- Message access
- Classification changes
- Data exports
- Failed authentication attempts
- Unauthorized access attempts

## Scenario: 3 Users

### User Configuration

```
User A (user123):
- Email: alice@company-a.com
- Gmail connected: alice@gmail.com
- Messages: 1,000
- Storage: 50MB

User B (user456):
- Email: bob@company-b.com
- Gmail connected: bob@gmail.com
- Messages: 500
- Storage: 25MB

User C (user789):
- Email: charlie@company-c.com
- Gmail connected: charlie@gmail.com
- Messages: 2,000
- Storage: 100MB
```

### Data Isolation in Practice

**Database:**
```sql
-- User A's view
SELECT * FROM extracted_messages;
-- Returns only rows where user_id = 'user123'

-- User B cannot see User A's data
SELECT * FROM extracted_messages WHERE user_id = 'user123';
-- Returns 0 rows (blocked by RLS policy)
```

**Redis:**
```
User A's keys:
- user:user123:dedup:gmail_msg_001
- user:user123:classification:msg_001

User B's keys:
- user:user456:dedup:gmail_msg_501
- user:user456:classification:msg_501

Complete isolation - no key overlap possible
```

**Kafka:**
```
Topic: raw-messages.extracted

Message from User A:
{
  "userId": "user123",  ← Encrypted with User A's key
  "messageId": "msg_001",
  "body": "..."
}

Consumer validates:
if (message.userId != currentUser.getId()) {
    skip(); // Cannot process other users' messages
}
```

## Security Best Practices

### 1. Principle of Least Privilege
- Users only get access to their own data
- Service accounts have minimal permissions
- Database users have restricted grants

### 2. Defense in Depth
- Multiple layers of security
- Failure in one layer doesn't compromise system

### 3. Encryption Everywhere
- TLS for all network communication
- Encrypted database fields
- Encrypted Kafka messages
- Encrypted backups

### 4. Regular Security Audits
- Penetration testing
- Code security reviews
- Dependency vulnerability scanning
- Access pattern analysis

### 5. Monitoring & Alerts
- Failed authentication attempts
- Unusual access patterns
- Cross-user access attempts
- Data export requests

## Threat Model

### Threat 1: Unauthorized Access
**Attack:** User A tries to access User B's messages

**Defense:**
1. JWT validation fails (wrong token)
2. Application code validates userId
3. Database RLS blocks query
4. Audit log records attempt
5. Alert triggered

**Result:** ✅ Attack blocked at multiple layers

### Threat 2: SQL Injection
**Attack:** Malicious input tries to read all users' data

**Defense:**
1. Parameterized queries (no raw SQL)
2. Input validation
3. Database RLS enforces user_id filter
4. Even if query executes, RLS limits results

**Result:** ✅ Only attacker's own data returned

### Threat 3: Compromised Database Backup
**Attack:** Backup file stolen

**Defense:**
1. Backups encrypted at rest
2. Sensitive fields encrypted in database
3. Encryption keys stored separately (KMS)
4. Access to backups logged and monitored

**Result:** ✅ Data unreadable without keys

### Threat 4: Insider Threat
**Attack:** Database admin tries to access user data

**Defense:**
1. Column-level encryption (admin sees encrypted data)
2. All access logged
3. Encryption keys in separate KMS
4. Regular audit reviews

**Result:** ✅ Access detected and logged

## Testing Privacy

### Automated Tests

```java
@Test
public void testUserIsolation() {
    // Create messages for different users
    createMessage("user123", "Message A");
    createMessage("user456", "Message B");

    // Authenticate as User A
    authenticateAs("user123");

    // Try to get all messages
    List<Message> messages = service.getMessages("user123");

    // Assert only User A's messages returned
    assertEquals(1, messages.size());
    assertEquals("user123", messages.get(0).getUserId());

    // Try to access User B's message (should fail)
    assertThrows(ForbiddenException.class, () -> {
        service.getMessage("user456", "message_b_id");
    });
}
```

## Implementation Checklist

- [ ] Implement JWT authentication
- [ ] Add userId to all database tables
- [ ] Enable PostgreSQL Row-Level Security
- [ ] Add userId validation in all API endpoints
- [ ] Implement Redis key namespacing
- [ ] Add Kafka message encryption
- [ ] Implement audit logging
- [ ] Add data export endpoint
- [ ] Add data deletion endpoint
- [ ] Set up monitoring and alerts
- [ ] Conduct security audit
- [ ] Load test with multiple concurrent users
- [ ] Document security policies
- [ ] Train team on security practices

## Conclusion

This multi-layered approach ensures:
1. **User A cannot see User B's data** (enforced at DB, app, and cache levels)
2. **Data encrypted** at rest and in transit
3. **All access audited** for compliance
4. **GDPR compliant** with data export and deletion
5. **Scalable** to thousands of users
6. **Cost-effective** using shared infrastructure with strong isolation

The system provides **bank-level security** while maintaining operational simplicity.
