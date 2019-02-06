import * as React from 'react';
import {uuid} from '../../../../types/Types';
import {GraphContents} from '../../../../usecases/report/reportModels';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {toGraphContents} from './graphContentsMapper';
import {
  allQuantities,
  ExistingReadings,
  Measurement,
  MeasurementResponses,
  Medium,
  Quantity,
  Reading
} from './measurementModels';

export interface MeasurementTableData {
  readings: ExistingReadings;
  quantities: Quantity[];
}

const orderedQuantities = (medium: Medium): Quantity[] =>
  medium in allQuantities ? allQuantities[medium] : [];

// TODO[!must!] convert this to use 'createSelector<>()'
export const groupMeasurementsByDate =
  (measurementPage: NormalizedPaginated<Measurement>, medium: Medium): MeasurementTableData => {
    const readings: ExistingReadings = {};

    const quantities: Quantity[] = orderedQuantities(medium);
    const quantitiesFoundInResponse: Set<Quantity> = new Set<Quantity>();

    if (measurementPage) {
      measurementPage.result.content.forEach((id: uuid) => {
        const measurement: Measurement = measurementPage.entities.measurements[id];

        const reading: Reading = readings[measurement.created] || {id: measurement.created, measurements: {}};

        reading.measurements[measurement.quantity] = measurement;
        readings[measurement.created] = reading;
        quantitiesFoundInResponse.add(measurement.quantity);
      });
    }

    return {
      readings,
      quantities: quantities.filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };

export const getGraphContents = (responses: MeasurementResponses): GraphContents =>
  React.useMemo<GraphContents>(() => toGraphContents(responses), [responses]);
