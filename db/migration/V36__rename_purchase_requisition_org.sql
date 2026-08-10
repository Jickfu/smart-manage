ALTER TABLE public.t_scm_purchase_requisition
    RENAME COLUMN apply_org_id TO org_id;

ALTER TABLE public.t_scm_purchase_requisition
    RENAME CONSTRAINT fk_scm_purchase_requisition_org TO fk_scm_purchase_requisition_org_id;

COMMENT ON COLUMN public.t_scm_purchase_requisition.org_id IS '单据所属组织ID';
