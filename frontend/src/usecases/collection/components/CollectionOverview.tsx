import * as React from 'react';
import {WrapperIndent} from '../../../components/layouts/wrapper/Wrapper';
import {PieChartSelector, PieChartSelectorProps} from '../../../components/pie-chart-selector/PieChartSelector';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {GatewayDataSummary} from '../../../state/domain-models-paginated/gateway/gatewayModels';
import {FilterParam, OnSelectParameter, ParameterName} from '../../../state/search/selection/selectionModels';
import {ItemOrArray} from '../../../types/Types';

interface CollectionOverviewProps {
  gatewayDataSummary: Maybe<GatewayDataSummary>;
  setSelection: OnSelectParameter;
}

const colors: string[][] = [
  ['#e8a090', '#fce8cc'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

export const CollectionOverview = ({gatewayDataSummary, setSelection}: CollectionOverviewProps) => {
  const selectStatus = (id: ItemOrArray<FilterParam>) =>
    setSelection({parameter: ParameterName.gatewayStatuses, id});

  const selectCity = (id: ItemOrArray<FilterParam>) =>
    setSelection({parameter: ParameterName.cities, id});

  const selectProductModel = (id: ItemOrArray<FilterParam>) =>
    setSelection({parameter: ParameterName.productModels, id});

  if (gatewayDataSummary.isNothing()) {
    return null;
  } else {
    const {status, flagged, location, productModel} = gatewayDataSummary.get();
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
        heading: translate('cities'),
        data: location,
        colors: colors[0],
        setSelection: selectCity,
        maxSlices: 4,
      },
      {
        heading: translate('product models'),
        data: productModel,
        colors: colors[1],
        setSelection: selectProductModel,
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
