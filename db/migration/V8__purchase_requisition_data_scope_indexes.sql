DROP INDEX idx_purchase_requisition_scope;

CREATE INDEX idx_purchase_requisition_scope_org
    ON t_scm_purchase_requisition(org_id, create_time DESC, id DESC);

CREATE INDEX idx_purchase_requisition_scope_applicant
    ON t_scm_purchase_requisition(applicant_id, create_time DESC, id DESC);
