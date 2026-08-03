ALTER TABLE public.t_sys_basic_data RENAME TO t_sys_basic_data_category;
ALTER TABLE public.t_sys_basic_data_entry RENAME TO t_sys_basic_data_item;

ALTER TABLE public.t_sys_basic_data_category
    RENAME CONSTRAINT uk_basic_data_number TO uk_basic_data_category_number;
ALTER TABLE public.t_sys_basic_data_item
    DROP CONSTRAINT uk_basic_data_entry_parent_number,
    DROP CONSTRAINT fk_basic_data_entry_parent;

ALTER TABLE public.t_sys_basic_data_category
    ADD COLUMN cloud_id bigint,
    ADD COLUMN system_preset boolean DEFAULT false NOT NULL;

UPDATE public.t_sys_basic_data_category SET cloud_id = 4 WHERE cloud_id IS NULL;

ALTER TABLE public.t_sys_basic_data_category
    ALTER COLUMN cloud_id SET NOT NULL,
    ADD CONSTRAINT fk_basic_data_category_cloud FOREIGN KEY (cloud_id) REFERENCES public.t_sys_cloud(id);

ALTER TABLE public.t_sys_basic_data_item RENAME COLUMN parent_id TO category_id;
ALTER TABLE public.t_sys_basic_data_item
    ADD COLUMN parent_id bigint,
    ADD COLUMN remark character varying(255),
    ADD COLUMN system_preset boolean DEFAULT false NOT NULL,
    ADD COLUMN level integer DEFAULT 1 NOT NULL,
    ADD COLUMN number_path character varying(1000),
    ADD COLUMN name_path character varying(2000),
    ADD COLUMN is_leaf boolean DEFAULT true NOT NULL,
    ADD COLUMN mutex integer DEFAULT 0 NOT NULL;

UPDATE public.t_sys_basic_data_item
SET number_path = number,
    name_path = name;

ALTER TABLE public.t_sys_basic_data_item
    ALTER COLUMN number_path SET NOT NULL,
    ALTER COLUMN name_path SET NOT NULL,
    ADD CONSTRAINT fk_basic_data_item_category FOREIGN KEY (category_id)
        REFERENCES public.t_sys_basic_data_category(id),
    ADD CONSTRAINT fk_basic_data_item_parent FOREIGN KEY (parent_id)
        REFERENCES public.t_sys_basic_data_item(id),
    ADD CONSTRAINT ck_basic_data_item_level CHECK (level >= 1);

DROP INDEX IF EXISTS public.idx_basic_data_entry_parent_id;
CREATE INDEX idx_basic_data_category_cloud_id ON public.t_sys_basic_data_category (cloud_id);
CREATE INDEX idx_basic_data_item_category_id ON public.t_sys_basic_data_item (category_id);
CREATE INDEX idx_basic_data_item_parent_id ON public.t_sys_basic_data_item (parent_id);
CREATE UNIQUE INDEX uk_basic_data_item_category_number
    ON public.t_sys_basic_data_item (category_id, number);

COMMENT ON TABLE public.t_sys_basic_data_category IS '基础资料分类';
COMMENT ON COLUMN public.t_sys_basic_data_category.cloud_id IS '所属云ID';
COMMENT ON COLUMN public.t_sys_basic_data_category.system_preset IS '是否系统预置';
COMMENT ON TABLE public.t_sys_basic_data_item IS '基础资料节点';
COMMENT ON COLUMN public.t_sys_basic_data_item.category_id IS '基础资料分类ID';
COMMENT ON COLUMN public.t_sys_basic_data_item.parent_id IS '上级基础资料ID';
COMMENT ON COLUMN public.t_sys_basic_data_item.remark IS '备注';
COMMENT ON COLUMN public.t_sys_basic_data_item.system_preset IS '是否系统预置';
COMMENT ON COLUMN public.t_sys_basic_data_item.level IS '级次';
COMMENT ON COLUMN public.t_sys_basic_data_item.number_path IS '长编码';
COMMENT ON COLUMN public.t_sys_basic_data_item.name_path IS '长名称';
COMMENT ON COLUMN public.t_sys_basic_data_item.is_leaf IS '是否叶子节点';
COMMENT ON COLUMN public.t_sys_basic_data_item.mutex IS '乐观锁版本号';
