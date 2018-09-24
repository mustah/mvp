import {normalize} from 'normalizr';
import {getMediumType, Medium} from '../../components/indicators/indicatorWidgetModels';
import {measurement} from '../../state/domain-models-paginated/meter/meterSchema';
import {DomainModel, Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {allQuantities, Measurement, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';
import {RenderableMeasurement} from './MeterDetailsTabs';

export const orderedQuantities = (medium: Medium): string[] => {
  return medium in allQuantities
    ? allQuantities[medium]
    : [];
};

export const meterMeasurementsForTable = (meter: MeterDetails): DomainModel<RenderableMeasurement> => {
  const normalized: Normalized<Measurement> = normalize(meter.measurements, measurement);
  const entities: ObjectsById<RenderableMeasurement> =
    normalized.entities.measurements ? {...normalized.entities.measurements} : {};
  const result: uuid[] = normalized.result;
  const orderedResult: uuid[] = [];

  orderedQuantities(getMediumType(meter.medium)).forEach((quantity) => {
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
