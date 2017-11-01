import {IdNamed, uuid} from '../../../types/Types';

export interface SelectionParameter extends IdNamed {
  parameter: parameterNames;
}

export interface SelectionEntity {
  [key: string]: IdNamed;
}

export interface SelectedIds {
  [key: string]: uuid[];
}

export interface SelectionNormalized {
  entities: SelectionEntity;
  result: SelectedIds;
}

export enum parameterNames {
  cities = 'cities',
  addresses = 'addresses',
  statuses = 'statuses',
  period = 'period',
}
