import * as React from 'react';
import {Loader} from '../../../../components/loading/Loader';
import {useFetchSelectionTree} from '../../../../state/selection-tree/fetchSelectionTreeHook';
import {getMeterIdsWithLimit} from '../../../../state/selection-tree/selectionTreeSelectors';
import {getGraphContents} from '../../../../state/ui/graph/measurement/measurementSelectors';
import {GraphContainer} from '../../containers/GraphContainer';
import {DispatchToProps, StateToProps} from '../../containers/GraphTabContainer';
import {GraphContents} from '../../reportModels';

export type Props = StateToProps & DispatchToProps;

export const Graph = ({
  clearError,
  fetchSelectionTree,
  fetchMeasurements,
  hiddenLines,
  measurement,
  parameters,
  requestParameters,
  selectionTree,
  showMetersInGraph
}: Props) => {
  useFetchSelectionTree({parameters, fetchSelectionTree});

  React.useEffect(() => {
    if (selectionTree.isSuccessfullyFetched) {
      showMetersInGraph(getMeterIdsWithLimit(selectionTree.entities.meters));
    }
  }, [selectionTree]);

  React.useEffect(() => {
    fetchMeasurements(requestParameters);
  }, [requestParameters, requestParameters.selectedListItems, selectionTree.isSuccessfullyFetched]);

  const graphContents: GraphContents = getGraphContents(measurement.measurementResponse);

  const isFetching = measurement.isFetching || selectionTree.isFetching;

  return (
    <Loader isFetching={isFetching} error={measurement.error} clearError={clearError}>
      <GraphContainer graphContents={graphContents} outerHiddenKeys={hiddenLines}/>
    </Loader>
  );
};
