import * as React from 'react';
import {createSelector} from 'reselect';
import {isDefined} from '../../../../helpers/commonHelpers';
import {uuid} from '../../../../types/Types';
import {GraphContents} from '../../../../usecases/report/reportModels';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {toGraphContents} from './graphContentsMapper';
import {
  allQuantities,
  ExistingReadings,
  Measurement,
  MeasurementResponse,
  MeasurementResponsePart,
  Medium,
  Quantity,
  Reading
} from './measurementModels';

export interface MeasurementTableData {
  readings: ExistingReadings;
  quantities: Quantity[];
}

export const groupMeasurementsByDate =
  (measurementPage: NormalizedPaginated<Measurement>, medium: Medium): MeasurementTableData => {
    const readings: ExistingReadings = {};
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
      quantities: allQuantities[medium].filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };

export const useGraphContents = (responses: MeasurementResponse): GraphContents =>
  React.useMemo<GraphContents>(() => toGraphContents(responses), [responses]);

export const hasMeasurements = createSelector<MeasurementResponse, MeasurementResponsePart[], boolean>(
  (measurements: MeasurementResponse) => measurements.measurements,
  (measurements) => measurements
                      .filter((measurement) =>
                        measurement.values.find((value) => value.value !== undefined)
                      ).filter(isDefined).length > 0
);
