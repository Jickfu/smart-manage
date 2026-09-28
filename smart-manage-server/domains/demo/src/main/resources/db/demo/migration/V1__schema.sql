--
-- Name: t_demo_purchase_requisition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_demo_purchase_requisition (
    id bigint NOT NULL,
    number character varying(64) NOT NULL,
    subject character varying(255) NOT NULL,
    org_id bigint NOT NULL,
    applicant_id bigint NOT NULL,
    biz_date date NOT NULL,
    required_date date,
    reason character varying(1000),
    bill_status character(1) DEFAULT 'A'::bpchar NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_demo_purchase_requisition_status CHECK ((bill_status = ANY (ARRAY['A'::bpchar, 'B'::bpchar, 'C'::bpchar, 'D'::bpchar])))
);


--
-- Name: TABLE t_demo_purchase_requisition; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_demo_purchase_requisition IS '采购申请';


--
-- Name: COLUMN t_demo_purchase_requisition.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.id IS 'ID';


--
-- Name: COLUMN t_demo_purchase_requisition.number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.number IS '编码';


--
-- Name: COLUMN t_demo_purchase_requisition.subject; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.subject IS '主题';


--
-- Name: COLUMN t_demo_purchase_requisition.org_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.org_id IS '单据所属组织ID';


--
-- Name: COLUMN t_demo_purchase_requisition.applicant_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.applicant_id IS '申请人ID';


--
-- Name: COLUMN t_demo_purchase_requisition.biz_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.biz_date IS '业务日期';


--
-- Name: COLUMN t_demo_purchase_requisition.required_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.required_date IS '需求日期';


--
-- Name: COLUMN t_demo_purchase_requisition.reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.reason IS '申请原因';


--
-- Name: COLUMN t_demo_purchase_requisition.bill_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.bill_status IS '单据状态：A暂存，B已提交，C已审核，D已关闭';


--
-- Name: COLUMN t_demo_purchase_requisition.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.version IS '乐观锁版本号';


--
-- Name: COLUMN t_demo_purchase_requisition.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.create_time IS '创建时间';


--
-- Name: COLUMN t_demo_purchase_requisition.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.update_time IS '更新时间';


--
-- Name: COLUMN t_demo_purchase_requisition.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.create_user IS '创建人';


--
-- Name: COLUMN t_demo_purchase_requisition.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition.update_user IS '修改人';


--
-- Name: t_demo_purchase_requisition_entry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_demo_purchase_requisition_entry (
    id bigint NOT NULL,
    parent_id bigint NOT NULL,
    material_name character varying(255) NOT NULL,
    specification character varying(255),
    unit character varying(32) NOT NULL,
    quantity numeric(19,6) NOT NULL,
    required_date date,
    remark character varying(500),
    sort integer DEFAULT 99 NOT NULL,
    create_time timestamp without time zone DEFAULT now(),
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_demo_purchase_requisition_entry_quantity CHECK ((quantity > (0)::numeric))
);


--
-- Name: TABLE t_demo_purchase_requisition_entry; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.t_demo_purchase_requisition_entry IS '采购申请明细';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.id IS 'ID';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.parent_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.parent_id IS '父级ID';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.material_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.material_name IS '物料名称';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.specification; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.specification IS '规格型号';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.unit IS '单位';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.quantity IS '数量';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.required_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.required_date IS '需求日期';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.remark; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.remark IS '备注';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.sort; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.sort IS '排序';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.create_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.create_time IS '创建时间';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.update_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.update_time IS '更新时间';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.create_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.create_user IS '创建人';


--
-- Name: COLUMN t_demo_purchase_requisition_entry.update_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.t_demo_purchase_requisition_entry.update_user IS '修改人';


--
-- Name: t_demo_purchase_requisition pk_demo_purchase_requisition; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition
    ADD CONSTRAINT pk_demo_purchase_requisition PRIMARY KEY (id);


--
-- Name: t_demo_purchase_requisition_entry pk_demo_purchase_requisition_entry; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition_entry
    ADD CONSTRAINT pk_demo_purchase_requisition_entry PRIMARY KEY (id);


--
-- Name: t_demo_purchase_requisition uk_demo_purchase_requisition_org_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition
    ADD CONSTRAINT uk_demo_purchase_requisition_org_number UNIQUE (org_id, number);


--
-- Name: idx_purchase_requisition_scope_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_purchase_requisition_scope_applicant ON public.t_demo_purchase_requisition USING btree (applicant_id, create_time DESC, id DESC);


--
-- Name: idx_purchase_requisition_scope_org; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_purchase_requisition_scope_org ON public.t_demo_purchase_requisition USING btree (org_id, create_time DESC, id DESC);


--
-- Name: idx_demo_purchase_requisition_entry_parent_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_demo_purchase_requisition_entry_parent_id ON public.t_demo_purchase_requisition_entry USING btree (parent_id);


--
-- Name: t_demo_purchase_requisition fk_demo_purchase_requisition_applicant; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition
    ADD CONSTRAINT fk_demo_purchase_requisition_applicant FOREIGN KEY (applicant_id) REFERENCES public.t_sys_user(id);


--
-- Name: t_demo_purchase_requisition_entry fk_demo_purchase_requisition_entry_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition_entry
    ADD CONSTRAINT fk_demo_purchase_requisition_entry_parent FOREIGN KEY (parent_id) REFERENCES public.t_demo_purchase_requisition(id);


--
-- Name: t_demo_purchase_requisition fk_demo_purchase_requisition_org_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_demo_purchase_requisition
    ADD CONSTRAINT fk_demo_purchase_requisition_org_id FOREIGN KEY (org_id) REFERENCES public.t_sys_org(id);


--
-- Name: t_demo_leave; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_demo_leave (
    id bigint PRIMARY KEY,
    number character varying(100) NOT NULL,
    client_key uuid NOT NULL UNIQUE,
    org_id bigint NOT NULL,
    applicant_id bigint NOT NULL,
    biz_date date NOT NULL,
    leave_type character varying(20) NOT NULL,
    start_time timestamp without time zone NOT NULL,
    end_time timestamp without time zone NOT NULL,
    days numeric(6,2) NOT NULL CHECK (days > 0),
    reason character varying(2000) NOT NULL,
    bill_status character(1) DEFAULT 'A'::bpchar NOT NULL,
    current_instance_id bigint,
    last_outcome character varying(20),
    version integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone,
    update_time timestamp without time zone,
    create_user bigint,
    update_user bigint,
    CONSTRAINT ck_demo_leave_period CHECK (end_time > start_time),
    CONSTRAINT ck_demo_leave_status CHECK (bill_status IN ('A', 'B', 'C', 'D')),
    CONSTRAINT uk_demo_leave_org_number UNIQUE (org_id, number)
);

COMMENT ON TABLE public.t_demo_leave IS '独立请假样板，不改变采购审批行为';
COMMENT ON COLUMN public.t_demo_leave.id IS 'ID';
COMMENT ON COLUMN public.t_demo_leave.number IS '编码';
COMMENT ON COLUMN public.t_demo_leave.client_key IS '客户端稳定建单标识';
COMMENT ON COLUMN public.t_demo_leave.org_id IS '所属组织ID';
COMMENT ON COLUMN public.t_demo_leave.applicant_id IS '申请人ID';
COMMENT ON COLUMN public.t_demo_leave.biz_date IS '业务日期';
COMMENT ON COLUMN public.t_demo_leave.leave_type IS '请假类型';
COMMENT ON COLUMN public.t_demo_leave.start_time IS '开始时间';
COMMENT ON COLUMN public.t_demo_leave.end_time IS '结束时间';
COMMENT ON COLUMN public.t_demo_leave.days IS '人工填写天数，不计算工作日或考勤余额';
COMMENT ON COLUMN public.t_demo_leave.reason IS '原因';
COMMENT ON COLUMN public.t_demo_leave.bill_status IS '单据状态：A暂存，B已提交，C已审核，D已关闭；拒绝或撤回回到A，结果记录在last_outcome';
COMMENT ON COLUMN public.t_demo_leave.current_instance_id IS '最近轮次；不建立跨领域外键';
COMMENT ON COLUMN public.t_demo_leave.last_outcome IS '最近轮次结果';
COMMENT ON COLUMN public.t_demo_leave.version IS '乐观锁版本';
COMMENT ON COLUMN public.t_demo_leave.create_time IS '创建时间';
COMMENT ON COLUMN public.t_demo_leave.update_time IS '更新时间';
COMMENT ON COLUMN public.t_demo_leave.create_user IS '创建人';
COMMENT ON COLUMN public.t_demo_leave.update_user IS '修改人';

CREATE INDEX idx_demo_leave_owner ON public.t_demo_leave (applicant_id, id DESC);
CREATE INDEX idx_demo_leave_org ON public.t_demo_leave (org_id, id DESC);

CREATE TABLE public.t_demo_leave_attachment_entry (
    id bigint PRIMARY KEY,
    parent_id bigint NOT NULL,
    attachment_id bigint NOT NULL,
    CONSTRAINT uk_demo_leave_attachment_entry UNIQUE (parent_id, attachment_id)
);

COMMENT ON TABLE public.t_demo_leave_attachment_entry IS '当前请假单选择的附件';
COMMENT ON COLUMN public.t_demo_leave_attachment_entry.id IS 'ID';
COMMENT ON COLUMN public.t_demo_leave_attachment_entry.parent_id IS '所属主单ID';
COMMENT ON COLUMN public.t_demo_leave_attachment_entry.attachment_id IS '附件ID';

CREATE TABLE public.t_demo_leave_attachment_snapshot (
    id bigint PRIMARY KEY,
    leave_id bigint NOT NULL,
    instance_id bigint NOT NULL,
    attachment_id bigint NOT NULL,
    CONSTRAINT uk_demo_leave_attachment_snapshot UNIQUE (instance_id, attachment_id)
);

COMMENT ON TABLE public.t_demo_leave_attachment_snapshot IS '每轮不可变附件引用，用于保留和精确授权';
COMMENT ON COLUMN public.t_demo_leave_attachment_snapshot.id IS 'ID';
COMMENT ON COLUMN public.t_demo_leave_attachment_snapshot.leave_id IS '请假单ID';
COMMENT ON COLUMN public.t_demo_leave_attachment_snapshot.instance_id IS '流程实例ID';
COMMENT ON COLUMN public.t_demo_leave_attachment_snapshot.attachment_id IS '附件ID';

CREATE INDEX idx_demo_leave_attachment_snapshot_file
    ON public.t_demo_leave_attachment_snapshot (leave_id, attachment_id);
