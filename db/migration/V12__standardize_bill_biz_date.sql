ALTER TABLE public.t_scm_purchase_requisition
    RENAME COLUMN apply_date TO biz_date;

COMMENT ON COLUMN public.t_scm_purchase_requisition.biz_date IS '业务日期';
