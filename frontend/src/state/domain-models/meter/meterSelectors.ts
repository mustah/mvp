
// TODO: Perhaps move this to domainSelectors
import {Meter, MetersState} from './meterModels';

export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): {[key: string]: Meter} => state.entities.meters;
