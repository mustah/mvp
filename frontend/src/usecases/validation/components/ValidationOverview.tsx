import * as React from 'react';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {PieChartSelector, PieChartSelectorProps} from '../../../components/pie-chart-selector/PieChartSelector';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {MeterDataSummary} from '../../../state/domain-models-paginated/meter/meterModels';
import {FilterParam, OnSelectParameter, ParameterName} from '../../../state/user-selection/userSelectionModels';
import {ItemOrArray} from '../../../types/Types';

// TODO: Perhaps move this to themes and make customizable.
const colors: string[][] = [
  ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

interface ValidationOverviewProps {
  meterDataSummary: Maybe<MeterDataSummary>;
  setSelection: OnSelectParameter;
}

export const ValidationOverview = (props: ValidationOverviewProps) => {
  const {
    setSelection,
    meterDataSummary,
  } = props;

  const selectStatus = (id: ItemOrArray<FilterParam>) => setSelection({parameter: ParameterName.meterStatuses, id});
  const selectCity = (id: ItemOrArray<FilterParam>) => setSelection({parameter: ParameterName.cities, id});
  const selectManufacturer = (id: ItemOrArray<FilterParam>) =>
    setSelection({parameter: ParameterName.manufacturers, id});
  const selectAlarm = (id: ItemOrArray<FilterParam>) => setSelection({parameter: ParameterName.alarms, id});

  if (meterDataSummary.isNothing()) {
    return null;
  } else {
    const {status, flagged, alarm, location, manufacturer, medium} = meterDataSummary.get();
    const pieCharts: PieChartSelectorProps[] = [
      {
        heading: translate('status'),
        data: status,
        colors: colors[0],
        setSelection: selectStatus,
        maxSlices: 4,
      },
      {
        heading: translate('flagged for action'),
        data: flagged,
        colors: colors[1],
        maxSlices: 4,
      },
      {
        heading: translate('alarm', {count: Object.keys(alarm).length}),
        data: alarm,
        colors: colors[0],
        setSelection: selectAlarm,
        maxSlices: 4,
      },
      {
        heading: translate('cities'),
        data: location,
        colors: colors[1],
        setSelection: selectCity,
        maxSlices: 4,
      },
      {
        heading: translate('manufacturer'),
        data: manufacturer,
        colors: colors[0],
        setSelection: selectManufacturer,
        maxSlices: 4,
      },
      {
        heading: translate('medium'),
        data: medium,
        colors: colors[1],
        maxSlices: 4,
      },
    ];
    return (
      <WrapperIndent>
        {pieCharts.map((pieChart: PieChartSelectorProps, index) => <PieChartSelector key={index} {...pieChart}/>)}
      </WrapperIndent>
    );
  }
};
