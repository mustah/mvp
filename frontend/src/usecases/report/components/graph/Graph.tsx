import * as React from 'react';
import {Loader} from '../../../../components/loading/Loader';
import {useFetchMeasurements} from '../../../../state/ui/graph/measurement/measurementHook';
import {getGraphContents} from '../../../../state/ui/graph/measurement/measurementSelectors';
import {LineChartsContainer} from '../../containers/LineChartsContainer';
import {DispatchToProps, StateToProps} from '../../containers/MeasurementsContainer';
import {GraphContents} from '../../reportModels';

export type Props = StateToProps & DispatchToProps;

export const Graph = (props: Props) => {
  const {
    clearError,
    hiddenLines,
    measurement: {error, isFetching, measurementResponse},
    selectionTree,
  } = props;
  useFetchMeasurements(props);

  const graphContents: GraphContents = getGraphContents(measurementResponse);

  return (
    <Loader isFetching={isFetching || selectionTree.isFetching} error={error} clearError={clearError}>
      <LineChartsContainer graphContents={graphContents} outerHiddenKeys={hiddenLines}/>
    </Loader>
  );
};
