export interface DataAnalysisState {
  title: string;
  records: DataAnalysisState[];
  error?: string;
  isFetching: boolean;
}
