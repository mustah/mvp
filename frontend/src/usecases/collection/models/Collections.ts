export interface CollectionState {
  title: string;
}

export interface CollectionState {
  title: string;
  records: CollectionState[];
  error?: string;
  isFetching: boolean;
}
