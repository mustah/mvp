import {Overwrite} from 'utility-types';
import {IdNamed, uuid} from '../../../types/Types';
import {Organisation} from '../organisation/organisationModels';

export interface MeterDefinition extends IdNamed {
  quantities: DisplayQuantity[];
  organisation?: Organisation;
  medium: Medium;
  autoApply: boolean;
}

export type MeterDefinitionMaybeId = Overwrite<MeterDefinition, {id?: uuid}>;

export type Medium = IdNamed;

export interface DisplayQuantity extends IdNamed, Grid {
  quantityName: string;
  consumption?: boolean;
  displayUnit: string;
  precision: number;
}

export type Quantity = IdNamed;

interface Grid {
  inEdit: boolean;
  gridIndex: number;
}
