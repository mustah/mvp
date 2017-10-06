export interface GraphRecord {
  name: string;
  value: number;
}

export interface DonutGraph {
  title: string;
  records: GraphRecord[];
}
