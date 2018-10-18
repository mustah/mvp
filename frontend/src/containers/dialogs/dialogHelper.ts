import {NormalizedPaginated} from '../../state/domain-models-paginated/paginatedDomainModels';
import {
  allQuantities,
  Measurement,
  Medium,
  Quantity,
  Reading
} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';

const orderedQuantities = (medium: Medium): Quantity[] =>
  medium in allQuantities
    ? allQuantities[medium]
    : [];

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
