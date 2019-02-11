import * as React from 'react';
import {Props} from '../../../../usecases/report/components/graph/Graph';
import {useFetchSelectionTree} from '../../../selection-tree/fetchSelectionTreeHook';
import {getMeterIdsWithLimit} from '../../../selection-tree/selectionTreeSelectors';

export const useFetchMeasurements = ({
  fetchSelectionTree,
  fetchMeasurements,
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
};
