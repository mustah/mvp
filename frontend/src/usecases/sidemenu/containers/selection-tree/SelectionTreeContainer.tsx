import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {FoldableMenuItem} from '../../../../components/layouts/foldable/Foldable';
import {RootState} from '../../../../reducers/rootReducer';
import {isDashboardPage, isReportPage} from '../../../../selectors/routerSelectors';
import {translate} from '../../../../services/translationService';
import {fetchSelectionTree} from '../../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../../../state/selection-tree/selectionTreeSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {getMeterParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch, OnClick, OnClickWithId, uuid} from '../../../../types/Types';
import {centerMapOnMeter} from '../../../dashboard/dashboardActions';
import {addToReport, toggleIncludingChildren, toggleSingleEntry} from '../../../report/reportActions';
import {LoadingListItem} from '../../components/LoadingListItem';
import {ItemOptions, renderSelectionTreeCities} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface StateToProps {
  isFetching: boolean;
  selectionTree: SelectionTree;
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

class SelectionTreeComponent extends React.Component<Props> {

  componentDidMount() {
    const {fetchSelectionTree, parameters} = this.props;
    fetchSelectionTree(parameters);
  }

  componentWillReceiveProps({fetchSelectionTree, parameters}: Props) {
    fetchSelectionTree(parameters);
  }

  render() {
    const {
      addToReport,
      isFetching,
      selectionTree,
      toggleExpand,
      openListItems,
      toggleIncludingChildren,
      toggleSingleEntry,
      itemOptions,
      centerMapOnMeter,
      primaryText,
    } = this.props;

    const renderSelectionTree = (id: uuid) =>
      renderSelectionTreeCities({
        addToReport,
        id,
        selectionTree,
        toggleExpand,
        openListItems,
        toggleIncludingChildren,
        toggleSingleEntry,
        itemOptions,
        centerMapOnMeter,
      });

    const cityIds: uuid[] = selectionTree.result.cities;
    const selectionTreeItems = cityIds.length
      ? [...cityIds].sort().map(renderSelectionTree)
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
  }
}

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
      selectionTree: getSelectionTree({...selectionTree}),
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
