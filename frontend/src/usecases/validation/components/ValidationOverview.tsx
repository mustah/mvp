import * as React from 'react';
import 'ValidationOverview.scss';
import {Row} from '../../../components/layouts/row/Row';
import {PieChartSelector, PieChartSelectorProps} from '../../../components/pie-chart-selector/PieChartSelector';
import {translate} from '../../../services/translationService';
import {MeterDataSummary} from '../../../state/domain-models/meter/meterModels';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {Maybe, uuid} from '../../../types/Types';

// TODO: Perhaps move this to themes and make customizable.
const colors: string[][] = [
  ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

interface ValidationOverviewProps {
  meterDataSummary: Maybe<MeterDataSummary>;
  addSelection: (searchParameters: SelectionParameter) => void;
}

export const ValidationOverview = (props: ValidationOverviewProps) => {
  const {
    addSelection,
    meterDataSummary,
  } = props;

  const selectStatus = (id: uuid) => addSelection({parameter: ParameterName.meterStatuses, id});
  const selectCity = (id: uuid) => addSelection({parameter: ParameterName.cities, id});
  const selectManufacturer = (id: uuid) => addSelection({parameter: ParameterName.manufacturers, id});
  const selectAlarm = (id: uuid) => addSelection({parameter: ParameterName.alarms, id});

  if (!meterDataSummary) {
    return null;
  } else {
    const pieCharts: PieChartSelectorProps[] = [
      {
        heading: translate('status'),
        data: meterDataSummary.status,
        colors: colors[0],
        onClick: selectStatus,
        maxSlices: 4,
      },
      {
        heading: translate('flagged for action'),
        data: meterDataSummary.flagged,
        colors: colors[1],
        maxSlices: 4,
      },
      {
        heading: translate('alarm', {count: Object.keys(meterDataSummary.alarm).length}),
        data: meterDataSummary.alarm,
        colors: colors[0],
        onClick: selectAlarm,
        maxSlices: 4,
      },
      {
        heading: translate('cities'),
        data: meterDataSummary.city,
        colors: colors[1],
        onClick: selectCity,
        maxSlices: 4,
      },
      {
        heading: translate('manufacturer'),
        data: meterDataSummary.manufacturer,
        colors: colors[0],
        onClick: selectManufacturer,
        maxSlices: 4,
      },
      {
        heading: translate('medium'),
        data: meterDataSummary.medium,
        colors: colors[1],
        maxSlices: 4,
      },
    ];
    return (
      <Row className="ValidationOverview">
        {pieCharts.map((pieChart: PieChartSelectorProps, index) => <PieChartSelector key={index} {...pieChart}/>)}
      </Row>
    );
  }
};
