import {Overwrite} from 'react-redux-typescript';
import {Identifiable, uuid} from '../../../types/Types';
import {Organisation} from '../organisation/organisationModels';

export interface MeterDefinition extends Identifiable {
  name: string;
  quantities: Quantity[];
  organisation?: Organisation;
  medium: Medium;
  autoApply: boolean;
}

export type MeterDefinitionMaybeId = Overwrite<MeterDefinition, {id?: uuid, quantities: QuantityMaybeId[]}>;

export interface Medium extends Identifiable {
  name: string;
}

export interface Quantity extends Identifiable {
  name: string;
  consumption?: boolean;
  displayUnit: string;
  precision: number;
}

export type QuantityMaybeId = Overwrite<Quantity, {id?: uuid, inEdit: boolean}>;
