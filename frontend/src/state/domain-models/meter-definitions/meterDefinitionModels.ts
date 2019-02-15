import {Overwrite} from 'react-redux-typescript';
import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {Organisation} from '../organisation/organisationModels';

export interface MeterDefinition extends Identifiable {
  name: string;
  quantities: DisplayQuantity[];
  organisation?: Organisation;
  medium: Medium;
  autoApply: boolean;
}

export type MeterDefinitionMaybeId = Overwrite<MeterDefinition, {id?: uuid, quantities: DisplayQuantity[]}>;

export interface Medium extends Identifiable {
  name: string;
}

export interface DisplayQuantity extends IdNamed, Grid {
  quantityName: string;
  consumption?: boolean;
  displayUnit: string;
  precision: number;
}

export interface Quantity extends Identifiable {
  name: string;
}

export interface Grid {
  inEdit: boolean;
  gridIndex: number;
}

export type DisplayQuantityMaybeId = Overwrite<DisplayQuantity, {id?: uuid}>;
