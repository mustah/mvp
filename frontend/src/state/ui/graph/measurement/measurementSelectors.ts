import {flatMap} from 'lodash';
import * as React from 'react';
import {createSelector} from 'reselect';
import {identity, isDefined} from '../../../../helpers/commonHelpers';
import {uuid} from '../../../../types/Types';
import {toGraphContents} from '../../../../usecases/report/helpers/lineChartHelper';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {GraphContents} from '../../../report/reportModels';
import {
  allQuantitiesMap,
  ExistingReadings,
  Measurement,
  MeasurementResponse,
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
      quantities: allQuantitiesMap[medium].filter((q) => quantitiesFoundInResponse.has(q)),
    };
  };

export const useGraphContents =
  (responses: MeasurementResponse, selectedQuantities: Quantity[]): GraphContents =>
    React.useMemo<GraphContents>(() => toGraphContents(responses, selectedQuantities), [responses, selectedQuantities]);

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({average, measurements}: MeasurementResponse) =>
    [
      ...flatMap(average, it => it.values).filter(it => isDefined(it.value)),
      ...flatMap(measurements, it => it.values).filter(it => isDefined(it.value)),
    ].length > 0
);
