import {EndPoints} from '../../../services/endPoints';
import {clearError, fetchEntityIfNeeded, fetchIfNeeded} from '../domainModelsActions';
import {Quantity} from '../meter-definitions/meterDefinitionModels';
import {quantitiesDataFormatter} from './quantitySchema';

export const fetchQuantities = fetchIfNeeded<Quantity>(
  EndPoints.quantities,
  'quantities',
  quantitiesDataFormatter,
);

export const fetchQuantity = fetchEntityIfNeeded(EndPoints.quantities, 'quantities');

export const clearQuantityErrors = clearError(EndPoints.quantities);
