import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {FoldableMenuItem} from '../../../../components/layouts/foldable/Foldable';
import {RootState} from '../../../../reducers/rootReducer';
import {isDashboardPage, isReportPage} from '../../../../selectors/routerSelectors';
import {translate} from '../../../../services/translationService';
import {fetchSelectionTree} from '../../../../state/selection-tree/selectionTreeApiActions';
import {NormalizedSelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {getMeterParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch, OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {centerMapOnMeter} from '../../../dashboard/dashboardActions';
import {addToReport, toggleIncludingChildren, toggleSingleEntry} from '../../../report/reportActions';
import {LoadingListItem} from '../../components/LoadingListItem';
import {ItemOptions, renderSelectionTreeCity} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface StateToProps {
  isFetching: boolean;
  selectionTree: NormalizedSelectionTree;
  openListItems: Set<uuid>;
  parameters: EncodedUriParameters;
  itemOptions: ItemOptions;
  primaryText: string;
}

interface DispatchToProps {
  addToReport: OnClickWithId;
  fetchSelectionTree: Fetch;
  toggleExpand: OnClickWithId;
  toggleSingleEntry: OnClickWithId;
  toggleIncludingChildren: OnClick;
  centerMapOnMeter: OnClickWithId;
}

type Props = StateToProps & DispatchToProps;

const SelectionTreeComponent = ({
  addToReport,
  fetchSelectionTree,
  isFetching,
  selectionTree,
  toggleExpand,
  openListItems,
  toggleIncludingChildren,
  toggleSingleEntry,
  itemOptions,
  centerMapOnMeter,
  parameters,
  primaryText,
}: Props) => {
  React.useEffect(() => {
    fetchSelectionTree(parameters);
  }, [parameters]);

  const cityIds: uuid[] = selectionTree.result.cities;
  const selectionTreeItems = cityIds.length
    ? [...cityIds].map((id: uuid) =>
      renderSelectionTreeCity({
        addToReport,
        id,
        selectionTree,
        toggleExpand,
        openListItems,
        toggleIncludingChildren,
        toggleSingleEntry,
        itemOptions,
        centerMapOnMeter,
      }))
    : [
      (
        <LoadingListItem
          isFetching={isFetching}
          text={translate('no meters')}
          key="loading-list-item"
        />
      )
    ];

  return (
    <FoldableMenuItem title={primaryText}>
      {selectionTreeItems}
    </FoldableMenuItem>
  );
};

const mapStateToProps =
  ({
    report,
    userSelection: {userSelection},
    selectionTree,
    ui: {selectionTree: selectionTreeUi},
    routing,
  }: RootState): StateToProps =>
    ({
      isFetching: selectionTree.isFetching,
      selectionTree,
      openListItems: getOpenListItems(selectionTreeUi),
      parameters: getMeterParameters({userSelection}),
      itemOptions: {
        zoomable: isDashboardPage(routing),
        report: isReportPage(routing),
      },
      primaryText: userSelection.name,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  centerMapOnMeter,
  addToReport,
  fetchSelectionTree,
  toggleExpand: selectionTreeToggleId,
  toggleSingleEntry,
  toggleIncludingChildren,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(SelectionTreeComponent);
