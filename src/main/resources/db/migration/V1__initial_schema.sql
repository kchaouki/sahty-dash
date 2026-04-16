-- Sahty EMR - Initial Database Schema
-- Version: 1.0.0

-- ===========================================
-- TENANT MANAGEMENT
-- ===========================================

CREATE TABLE IF NOT EXISTS tenant_groups (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS tenants (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(255),
    db_name VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    group_id VARCHAR(36) REFERENCES tenant_groups(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ===========================================
-- USERS & ROLES
-- ===========================================

CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    tenant_id VARCHAR(36) REFERENCES tenants(id)
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id VARCHAR(36) REFERENCES roles(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    system_role VARCHAR(30) NOT NULL DEFAULT 'TENANT_USER',
    tenant_id VARCHAR(36) REFERENCES tenants(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id VARCHAR(36) REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(36) REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS user_permissions (
    user_id VARCHAR(36) REFERENCES users(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, permission)
);

-- ===========================================
-- REFERENCE DATA
-- ===========================================

CREATE TABLE IF NOT EXISTS organisms (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    type VARCHAR(50),
    country VARCHAR(50),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- ===========================================
-- HOSPITAL STRUCTURE
-- ===========================================

CREATE TABLE IF NOT EXISTS hospital_services (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    color VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS rooms (
    id VARCHAR(36) PRIMARY KEY,
    service_id VARCHAR(36) NOT NULL REFERENCES hospital_services(id),
    name VARCHAR(100) NOT NULL,
    floor VARCHAR(20),
    capacity INT DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS beds (
    id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(36) NOT NULL REFERENCES rooms(id),
    label VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_patient_id VARCHAR(36),
    current_admission_id VARCHAR(36)
);

CREATE TABLE IF NOT EXISTS user_services (
    user_id VARCHAR(36) REFERENCES users(id) ON DELETE CASCADE,
    service_id VARCHAR(36) REFERENCES hospital_services(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, service_id)
);

-- ===========================================
-- PATIENTS
-- ===========================================

CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(36) PRIMARY KEY,
    tenant_patient_id VARCHAR(36),
    global_patient_id VARCHAR(36),
    tenant_id VARCHAR(36) NOT NULL REFERENCES tenants(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    ipp VARCHAR(50),
    lifecycle_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    identity_status VARCHAR(20) DEFAULT 'PROVISIONAL',
    phone VARCHAR(50),
    email VARCHAR(255),
    home_phone VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(50),
    nationality VARCHAR(100),
    marital_status VARCHAR(50),
    profession VARCHAR(100),
    blood_group VARCHAR(10),
    father_name VARCHAR(200),
    mother_name VARCHAR(200),
    father_phone VARCHAR(50),
    mother_phone VARCHAR(50),
    is_payant BOOLEAN DEFAULT true,
    main_org_id VARCHAR(36),
    main_org_relationship VARCHAR(50),
    main_org_registration_number VARCHAR(100),
    complementary_org_id VARCHAR(36),
    guardian_first_name VARCHAR(100),
    guardian_last_name VARCHAR(100),
    guardian_phone VARCHAR(50),
    guardian_relationship VARCHAR(50),
    guardian_id_type VARCHAR(50),
    guardian_id_number VARCHAR(100),
    guardian_address TEXT,
    guardian_habilitation VARCHAR(100),
    merged_into_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS identity_documents (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    document_type VARCHAR(30) NOT NULL,
    document_number VARCHAR(100) NOT NULL,
    issuing_country VARCHAR(50),
    is_primary BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS emergency_contacts (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    relationship VARCHAR(50),
    phone VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS coverages (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    organism_id VARCHAR(36) REFERENCES organisms(id),
    policy_number VARCHAR(100),
    group_number VARCHAR(100),
    plan_name VARCHAR(200),
    coverage_type VARCHAR(20),
    effective_from DATE,
    effective_to DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    relationship_to_subscriber VARCHAR(20) DEFAULT 'SELF'
);

CREATE TABLE IF NOT EXISTS antecedents (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    icd_code VARCHAR(20),
    icd_description VARCHAR(500),
    year_onset INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS allergies (
    id VARCHAR(36) PRIMARY KEY,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    allergen VARCHAR(200) NOT NULL,
    allergen_type VARCHAR(20),
    severity VARCHAR(30),
    reactions TEXT,
    notes TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ===========================================
-- ADMISSIONS
-- ===========================================

CREATE TABLE IF NOT EXISTS admissions (
    id VARCHAR(36) PRIMARY KEY,
    nda VARCHAR(50) UNIQUE,
    patient_id VARCHAR(36) NOT NULL REFERENCES patients(id),
    tenant_id VARCHAR(36) NOT NULL REFERENCES tenants(id),
    service_id VARCHAR(36) REFERENCES hospital_services(id),
    room_id VARCHAR(36) REFERENCES rooms(id),
    bed_label VARCHAR(50),
    bed_id VARCHAR(36),
    reason TEXT,
    doctor_name VARCHAR(200),
    doctor_id VARCHAR(36),
    type VARCHAR(30),
    arrival_mode VARCHAR(30),
    provenance VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    admission_date TIMESTAMP,
    discharge_date TIMESTAMP,
    currency VARCHAR(5) DEFAULT 'MAD',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ===========================================
-- CLINICAL DATA
-- ===========================================

CREATE TABLE IF NOT EXISTS clinical_exams (
    id VARCHAR(36) PRIMARY KEY,
    admission_id VARCHAR(36) NOT NULL REFERENCES admissions(id),
    examiner_id VARCHAR(36) REFERENCES users(id),
    temperature DECIMAL(5,2),
    heart_rate INT,
    systolic_b_p INT,
    diastolic_b_p INT,
    respiratory_rate INT,
    oxygen_saturation INT,
    weight DECIMAL(6,2),
    height DECIMAL(5,2),
    bmi DECIMAL(5,2),
    pain_score VARCHAR(10),
    glasgow_eye INT,
    glasgow_verbal INT,
    glasgow_motor INT,
    general_state TEXT,
    cardiac_exam TEXT,
    pulmonary_exam TEXT,
    abdominal_exam TEXT,
    neurological_exam TEXT,
    other_findings TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS observations (
    id VARCHAR(36) PRIMARY KEY,
    admission_id VARCHAR(36) NOT NULL REFERENCES admissions(id),
    author_id VARCHAR(36) REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    content TEXT,
    rich_content TEXT,
    is_confidential BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prescriptions (
    id VARCHAR(36) PRIMARY KEY,
    admission_id VARCHAR(36) NOT NULL REFERENCES admissions(id),
    prescriber_id VARCHAR(36) REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    molecule VARCHAR(200),
    commercial_name VARCHAR(200),
    dci_id VARCHAR(36),
    product_id VARCHAR(36),
    qty DECIMAL(10,4),
    unit VARCHAR(50),
    route VARCHAR(100),
    admin_mode VARCHAR(100),
    admin_duration DECIMAL(10,2),
    admin_duration_unit VARCHAR(20),
    schedule_mode VARCHAR(20),
    schedule_type VARCHAR(30),
    interval INT,
    interval_unit VARCHAR(20),
    start_date_time TIMESTAMP,
    duration_days INT,
    act_id VARCHAR(36),
    act_name VARCHAR(200),
    laboratory_section VARCHAR(100),
    imaging_type VARCHAR(100),
    notes TEXT,
    blood_product VARCHAR(100),
    blood_group VARCHAR(10),
    transfusion_units INT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    paused_at TIMESTAMP,
    stopped_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transfusions (
    id VARCHAR(36) PRIMARY KEY,
    admission_id VARCHAR(36) NOT NULL REFERENCES admissions(id),
    nurse_id VARCHAR(36) REFERENCES users(id),
    blood_product VARCHAR(100),
    blood_group VARCHAR(10),
    rh_factor VARCHAR(5),
    bag_number VARCHAR(50),
    volume_ml INT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PLANNED',
    pre_transfusion_notes TEXT,
    post_transfusion_notes TEXT,
    adverse_reaction BOOLEAN DEFAULT false,
    adverse_reaction_details TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS interventions (
    id VARCHAR(36) PRIMARY KEY,
    admission_id VARCHAR(36) NOT NULL REFERENCES admissions(id),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(100),
    surgeon_name VARCHAR(200),
    anesthesiologist VARCHAR(200),
    operating_room VARCHAR(100),
    planned_date TIMESTAMP,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    pre_op_notes TEXT,
    operative_report TEXT,
    post_op_notes TEXT,
    complications TEXT,
    anesthesia_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ===========================================
-- PHARMACY
-- ===========================================

CREATE TABLE IF NOT EXISTS suppliers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(255),
    contact_person VARCHAR(200),
    tax_id VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    tenant_id VARCHAR(36)
);

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    atc_code VARCHAR(20),
    dci_id VARCHAR(36),
    dci_name VARCHAR(255),
    form VARCHAR(100),
    dosage VARCHAR(100),
    dosage_unit VARCHAR(50),
    packaging_unit VARCHAR(50),
    units_per_pack INT,
    care_category_id VARCHAR(36),
    therapeutic_class VARCHAR(200),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    tenant_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product_suppliers (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    supplier_id VARCHAR(36) NOT NULL REFERENCES suppliers(id),
    purchase_price DECIMAL(12,4),
    currency VARCHAR(5) DEFAULT 'MAD',
    supplier_ref VARCHAR(100),
    price_valid_from DATE,
    price_valid_to DATE,
    is_preferred BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS stock_locations (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tenant_id VARCHAR(36) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'PHYSICAL',
    scope VARCHAR(20) NOT NULL DEFAULT 'PHARMACY',
    location_class VARCHAR(20) DEFAULT 'COMMERCIAL',
    valuation_policy VARCHAR(20) DEFAULT 'VALUABLE',
    service_id VARCHAR(36) REFERENCES hospital_services(id),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS stock_items (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    location_id VARCHAR(36) NOT NULL REFERENCES stock_locations(id),
    lot VARCHAR(100),
    expiration_date DATE,
    serial VARCHAR(100),
    container_type VARCHAR(20) NOT NULL,
    quantity_units INT,
    units_per_box INT,
    remaining_units INT,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    origin_sealed_box_id VARCHAR(36),
    origin_open_box_id VARCHAR(36),
    origin VARCHAR(30),
    unit_cost DECIMAL(12,4),
    supplier_id VARCHAR(36) REFERENCES suppliers(id),
    purchase_order_ref VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    location_id VARCHAR(36) REFERENCES stock_locations(id),
    movement_type VARCHAR(30) NOT NULL,
    quantity INT,
    lot VARCHAR(100),
    unit_cost DECIMAL(12,4),
    user_id VARCHAR(36) REFERENCES users(id),
    admission_id VARCHAR(36),
    patient_id VARCHAR(36),
    reference VARCHAR(200),
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    supplier_id VARCHAR(36) REFERENCES suppliers(id),
    status VARCHAR(30) DEFAULT 'DRAFT',
    order_date DATE,
    expected_delivery_date DATE,
    total_amount DECIMAL(14,4),
    currency VARCHAR(5) DEFAULT 'MAD',
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS purchase_order_lines (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL REFERENCES purchase_orders(id),
    product_id VARCHAR(36) NOT NULL REFERENCES products(id),
    ordered_quantity INT,
    received_quantity INT DEFAULT 0,
    unit_price DECIMAL(12,4),
    currency VARCHAR(5) DEFAULT 'MAD'
);

-- ===========================================
-- LIMS
-- ===========================================

CREATE TABLE IF NOT EXISTS lab_sections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_sub_sections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    section_id VARCHAR(36) NOT NULL REFERENCES lab_sections(id),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_methods (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    principle TEXT,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_specimens (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    description TEXT,
    collection_instructions TEXT,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_containers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    color VARCHAR(50),
    additive_type VARCHAR(100),
    volume VARCHAR(50),
    description TEXT,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_analytes (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    unit VARCHAR(50),
    normal_range_min VARCHAR(50),
    normal_range_max VARCHAR(50),
    critical_range_min VARCHAR(50),
    critical_range_max VARCHAR(50),
    description TEXT,
    section_id VARCHAR(36) REFERENCES lab_sections(id),
    sub_section_id VARCHAR(36) REFERENCES lab_sub_sections(id),
    method_id VARCHAR(36) REFERENCES lab_methods(id),
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS lab_samples (
    id VARCHAR(36) PRIMARY KEY,
    tenant_id VARCHAR(36) NOT NULL,
    sample_number VARCHAR(50) NOT NULL UNIQUE,
    patient_id VARCHAR(36) REFERENCES patients(id),
    admission_id VARCHAR(36) REFERENCES admissions(id),
    specimen_id VARCHAR(36) REFERENCES lab_specimens(id),
    container_id VARCHAR(36) REFERENCES lab_containers(id),
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    collection_date_time TIMESTAMP,
    reception_date_time TIMESTAMP,
    collector_id VARCHAR(36) REFERENCES users(id),
    receiver_id VARCHAR(36) REFERENCES users(id),
    barcode VARCHAR(100),
    notes TEXT,
    is_stat BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lab_results (
    id VARCHAR(36) PRIMARY KEY,
    sample_id VARCHAR(36) NOT NULL REFERENCES lab_samples(id),
    analyte_id VARCHAR(36) NOT NULL REFERENCES lab_analytes(id),
    value VARCHAR(200),
    unit VARCHAR(50),
    reference_range VARCHAR(100),
    flag VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING',
    validated_by_id VARCHAR(36) REFERENCES users(id),
    validated_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ===========================================
-- INDEXES
-- ===========================================

CREATE INDEX IF NOT EXISTS idx_patients_tenant ON patients(tenant_id);
CREATE INDEX IF NOT EXISTS idx_patients_ipp ON patients(ipp);
CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_admissions_tenant ON admissions(tenant_id);
CREATE INDEX IF NOT EXISTS idx_admissions_patient ON admissions(patient_id);
CREATE INDEX IF NOT EXISTS idx_admissions_status ON admissions(status);
CREATE INDEX IF NOT EXISTS idx_prescriptions_admission ON prescriptions(admission_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_tenant_product ON stock_items(tenant_id, product_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_location ON stock_items(location_id);
CREATE INDEX IF NOT EXISTS idx_stock_items_expiration ON stock_items(expiration_date);
CREATE INDEX IF NOT EXISTS idx_lab_samples_tenant ON lab_samples(tenant_id);
CREATE INDEX IF NOT EXISTS idx_lab_samples_patient ON lab_samples(patient_id);
CREATE INDEX IF NOT EXISTS idx_lab_results_sample ON lab_results(sample_id);

-- ===========================================
-- INITIAL DATA
-- ===========================================

-- Default Super Admin
INSERT INTO users (id, username, password_hash, first_name, last_name, system_role, active)
VALUES (
    'admin-00000000-0000-0000-0000-000000000001',
    'superadmin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
    'Super',
    'Admin',
    'SUPER_ADMIN',
    true
) ON CONFLICT (username) DO NOTHING;

-- Demo Organisms
INSERT INTO organisms (id, name, code, type, country, is_active) VALUES
    ('org-cnss-001', 'CNSS', 'CNSS', 'CNSS', 'MA', true),
    ('org-cnops-001', 'CNOPS', 'CNOPS', 'CNOPS', 'MA', true),
    ('org-ramed-001', 'RAMED', 'RAMED', 'RAMED', 'MA', true)
ON CONFLICT DO NOTHING;
