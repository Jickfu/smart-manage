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

COMMENT ON COLUMN public.t_demo_purchase_requisition.bill_status IS '单据状态：A暂存，B已提交，C审核通过，D已关闭';


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
