UPDATE public.t_sys_file_config
SET local_dir = CASE local_dir
        WHEN 'E:/upload/' THEN 'E:/smfiles/'
        WHEN './upload/' THEN './smfiles/'
    END,
    version = version + 1,
    update_time = CURRENT_TIMESTAMP
WHERE storage_type = 'LOCAL'
  AND local_dir IN ('E:/upload/', './upload/');
