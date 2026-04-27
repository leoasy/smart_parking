-- ============================================
-- Database Index Optimization for smart_parking
-- Generated: 2026-04-27
-- ============================================

-- ============================================
-- 1. ai_event - Add del_flag index
-- Purpose: Soft-delete queries filtering by del_flag
-- ============================================
ALTER TABLE ai_event ADD INDEX idx_ai_event_del_flag (del_flag);

-- ============================================
-- 2. biz_region - Add composite index for hierarchical queries
-- Purpose: Queries filtering by parent_id and level simultaneously
-- ============================================
ALTER TABLE biz_region ADD INDEX idx_biz_region_parent_level (parent_id, level);

-- ============================================
-- 3. biz_alarm - Add foreign key indexes
-- Purpose: camera_id and slot_id are foreign keys but lack dedicated indexes
-- ============================================
ALTER TABLE biz_alarm ADD INDEX idx_biz_alarm_camera_id (camera_id);
ALTER TABLE biz_alarm ADD INDEX idx_biz_alarm_slot_id (slot_id);

-- ============================================
-- 4. sys_user - Add composite index for common user lookup patterns
-- Purpose: Queries filtering by dept_id, status, and del_flag
-- ============================================
ALTER TABLE sys_user ADD INDEX idx_sys_user_dept_status_del (dept_id, status, del_flag);

-- ============================================
-- 5. sys_logininfor - Add ipaddr index
-- Purpose: Login location queries by IP address
-- ============================================
ALTER TABLE sys_logininfor ADD INDEX idx_sys_logininfor_ipaddr (ipaddr);

-- ============================================
-- 6. sys_oper_log - Add oper_name and oper_ip indexes
-- Purpose: Audit log queries by operator name or IP
-- ============================================
ALTER TABLE sys_oper_log ADD INDEX idx_sys_oper_log_oper_name (oper_name);
ALTER TABLE sys_oper_log ADD INDEX idx_sys_oper_log_oper_ip (oper_ip);

-- ============================================
-- Verification Query - Check all indexes after creation
-- ============================================
-- Run: SHOW INDEX FROM <table_name> for each modified table
