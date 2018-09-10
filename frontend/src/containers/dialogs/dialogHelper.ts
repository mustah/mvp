import {normalize} from 'normalizr';
import {getMediumType} from '../../components/indicators/indicatorWidgetModels';
import {translate} from '../../services/translationService';
import {measurement} from '../../state/domain-models-paginated/meter/meterSchema';
import {DomainModel, Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {
  allQuantities,
  Measurement,
  Quantity,
} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';
import {RenderableMeasurement} from './MeterDetailsTabs';

export const titleOf = (flags: Flag[]): string => {
  if (flags.length) {
    return flags.map((flag) => flag.title).join(', ');
  } else {
    return translate('no');
  }
};

const orderedQuantities = (medium: string): string[] => {
  return getMediumType(medium) in allQuantities
    ? allQuantities[getMediumType(medium)]
    : [];
};

export const meterMeasurementsForTable = (meter: MeterDetails): DomainModel<RenderableMeasurement> => {
  const normalized: Normalized<Measurement> = normalize(meter.measurements, measurement);
  const entities: ObjectsById<RenderableMeasurement> =
    normalized.entities.measurements ? {...normalized.entities.measurements} : {};
  const result: uuid[] = normalized.result;
  const orderedResult: uuid[] = [];

  orderedQuantities(meter.medium).forEach((quantity) => {
    if (!result.includes(quantity)) {
      entities[quantity] = {
        id: quantity,
        quantity: quantity as Quantity,
      };
    }
    orderedResult.push(quantity);
  });

  return {
    entities,
    result: orderedResult,
  };
};
