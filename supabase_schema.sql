-- ==========================================
-- XB Labs CRM Supabase Database Schema
-- Run this in your Supabase SQL Editor
-- ==========================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL UNIQUE,
    role TEXT NOT NULL DEFAULT 'EMPLOYEE',
    password_hash TEXT NOT NULL,
    theme TEXT NOT NULL DEFAULT 'default',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    last_active BIGINT NOT NULL
);

-- 2. Clients Table (Business Leads)
CREATE TABLE IF NOT EXISTS public.clients (
    id TEXT PRIMARY KEY,
    business_name TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'General',
    rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count INT NOT NULL DEFAULT 0,
    phone TEXT NOT NULL,
    normalized_phone TEXT NOT NULL,
    website TEXT,
    address TEXT,
    maps_url TEXT,
    assigned_employee_id TEXT REFERENCES public.users(id) ON DELETE SET NULL,
    assigned_employee_name TEXT,
    current_status TEXT NOT NULL DEFAULT 'NEW',
    employee_workflow_status TEXT NOT NULL DEFAULT 'ACTIVE',
    pipeline_stage TEXT NOT NULL DEFAULT 'NEW_LEAD',
    priority TEXT NOT NULL DEFAULT 'NORMAL',
    follow_up_count INT NOT NULL DEFAULT 0,
    next_follow_up_date BIGINT,
    last_contacted_at BIGINT,
    created_at BIGINT NOT NULL,
    imported_at BIGINT NOT NULL,
    import_batch_id TEXT NOT NULL,
    archived_at BIGINT
);

-- 3. Call Records Table
CREATE TABLE IF NOT EXISTS public.call_records (
    id TEXT PRIMARY KEY,
    client_id TEXT NOT NULL REFERENCES public.clients(id) ON DELETE CASCADE,
    client_name TEXT NOT NULL,
    employee_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    employee_name TEXT NOT NULL,
    status_outcome TEXT NOT NULL,
    notes TEXT,
    timestamp BIGINT NOT NULL,
    follow_up_number INT NOT NULL DEFAULT 0
);

-- 4. Follow Ups Table
CREATE TABLE IF NOT EXISTS public.follow_ups (
    id TEXT PRIMARY KEY,
    client_id TEXT NOT NULL REFERENCES public.clients(id) ON DELETE CASCADE,
    client_name TEXT NOT NULL,
    client_phone TEXT NOT NULL,
    employee_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    employee_name TEXT NOT NULL,
    attempt_number INT NOT NULL DEFAULT 1,
    due_date BIGINT NOT NULL,
    state TEXT NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    previous_result TEXT,
    created_at BIGINT NOT NULL,
    completed_at BIGINT
);

-- 5. Import Batches Table
CREATE TABLE IF NOT EXISTS public.import_batches (
    id TEXT PRIMARY KEY,
    admin_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    admin_name TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    source_name TEXT NOT NULL DEFAULT 'JSON Import',
    total_records INT NOT NULL DEFAULT 0,
    imported_records INT NOT NULL DEFAULT 0,
    duplicate_records INT NOT NULL DEFAULT 0,
    invalid_records INT NOT NULL DEFAULT 0,
    distribution_summary TEXT
);

-- 6. Notifications Table
CREATE TABLE IF NOT EXISTS public.notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL
);

-- 7. Activity Logs Table
CREATE TABLE IF NOT EXISTS public.activity_logs (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    user_name TEXT NOT NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    metadata TEXT,
    timestamp BIGINT NOT NULL
);

-- Enable Row Level Security (RLS) with open public access policies for API key connection
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.call_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follow_ups ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.import_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow All Users Read" ON public.users FOR SELECT USING (true);
CREATE POLICY "Allow All Users Insert" ON public.users FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow All Users Update" ON public.users FOR UPDATE USING (true);

CREATE POLICY "Allow All Clients Read" ON public.clients FOR SELECT USING (true);
CREATE POLICY "Allow All Clients Insert" ON public.clients FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow All Clients Update" ON public.clients FOR UPDATE USING (true);

CREATE POLICY "Allow All CallRecords Read" ON public.call_records FOR SELECT USING (true);
CREATE POLICY "Allow All CallRecords Insert" ON public.call_records FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow All FollowUps Read" ON public.follow_ups FOR SELECT USING (true);
CREATE POLICY "Allow All FollowUps Insert" ON public.follow_ups FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow All FollowUps Update" ON public.follow_ups FOR UPDATE USING (true);

CREATE POLICY "Allow All ImportBatches Read" ON public.import_batches FOR SELECT USING (true);
CREATE POLICY "Allow All ImportBatches Insert" ON public.import_batches FOR INSERT WITH CHECK (true);

CREATE POLICY "Allow All Notifications Read" ON public.notifications FOR SELECT USING (true);
CREATE POLICY "Allow All Notifications Insert" ON public.notifications FOR INSERT WITH CHECK (true);
CREATE POLICY "Allow All Notifications Update" ON public.notifications FOR UPDATE USING (true);

CREATE POLICY "Allow All ActivityLogs Read" ON public.activity_logs FOR SELECT USING (true);
CREATE POLICY "Allow All ActivityLogs Insert" ON public.activity_logs FOR INSERT WITH CHECK (true);

-- Initial Owner Seed (Xavier) & Demo Employee (Blessi)
INSERT INTO public.users (id, name, email, username, role, password_hash, theme, active, created_at, last_active)
VALUES 
    ('usr_admin_xavier', 'Xavier', 'xavier@xblabs.com', 'xavier', 'ADMIN', 'xblabs123@@@@', 'default', true, 1727380000000, 1727380000000),
    ('usr_emp_blessi', 'Blessi', 'blessi@xblabs.com', 'blessi', 'EMPLOYEE', 'xblabs123@', 'pink-princess', true, 1727380000000, 1727380000000)
ON CONFLICT (id) DO NOTHING;
