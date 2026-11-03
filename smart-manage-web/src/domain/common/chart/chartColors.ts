/** 首批分类色板；与界面品牌色独立，不随换肤改变数据系列含义。 */
export const chartCategoryColors = [
  '#0E5FD8',
  '#0F766E',
  '#8B5CF6',
  '#B45309',
  '#BE185D',
  '#475569',
] as const;

/** 明确任务状态使用语义色，不借用通用分类的数组位置。 */
export const chartTaskStatusColors = {
  success: '#1BA854',
  failed: '#FB2323',
  skipped: '#FF991C',
};
