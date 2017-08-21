export interface ValidationState {
  title: string;
}

export interface ValidationState {
  title: string;
  records: ValidationState[];
  error?: string;
  isFetching: boolean;
}
