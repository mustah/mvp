import {normalize} from 'normalizr';
import {measurement} from '../../state/domain-models-paginated/meter/meterSchema';
import {NormalizedPaginated} from '../../state/domain-models-paginated/paginatedDomainModels';
import {DomainModel, Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {
  allQuantities,
  getMediumType,
  Measurement,
  Medium,
  Quantity,
  Reading
} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';
import {RenderableMeasurement} from './MeterDetailsTabs';

const orderedQuantities = (medium: Medium): Quantity[] => {
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

export interface MeasurementTableData {
  readings: Map<number, Reading>;
  quantities: Quantity[];
}

export const groupMeasurementsByDate =
  (measurementPage: NormalizedPaginated<Measurement>, medium: Medium): MeasurementTableData => {
    const readings: Map<number, Reading> = new Map<number, Reading>();

    const quantities: Quantity[] = orderedQuantities(medium);
    const quantitiesFoundInResponse: Set<Quantity> = new Set<Quantity>();

    if (measurementPage) {
      measurementPage.result.content.forEach((id: uuid) => {
        const measurement: Measurement = measurementPage.entities.measurements[id];

        const reading: Reading =
          readings.get(measurement.created) || {id: measurement.created, measurements: {}};

        reading.measurements[measurement.quantity] = measurement;
        quantitiesFoundInResponse.add(measurement.quantity);
        readings.set(measurement.created, reading);
      });
    }

    return {
      readings,
      quantities: quantities.filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };
