import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {withContent} from '../../../../components/hoc/withContent';
import {Column} from '../../../../components/layouts/column/Column';
import {FoldableMenuItem} from '../../../../components/layouts/foldable/FoldableMenuItem';
import {SearchBox} from '../../../../components/search-box/SearchBox';
import {RootState} from '../../../../reducers/rootReducer';
import {isDashboardPage, isReportPage} from '../../../../selectors/routerSelectors';
import {translate} from '../../../../services/translationService';
import {fetchSelectionTree} from '../../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTree} from '../../../../state/selection-tree/selectionTreeModels';
import {getMeterIdsWithLimit, getSelectionTree} from '../../../../state/selection-tree/selectionTreeSelectors';
import {selectionTreeToggleId} from '../../../../state/ui/selection-tree/selectionTreeActions';
import {getOpenListItems} from '../../../../state/ui/selection-tree/selectionTreeSelectors';
import {getMeterParameters} from '../../../../state/user-selection/userSelectionSelectors';
import {
  CallbackWithIds,
  Clickable,
  EncodedUriParameters,
  Fetch,
  HasContent,
  OnChange,
  OnClick,
  OnClickWithId,
  uuid
} from '../../../../types/Types';
import {centerMapOnMeter} from '../../../dashboard/dashboardActions';
import {
  addToReport,
  showMetersInGraph,
  toggleIncludingChildren,
  toggleSingleEntry
} from '../../../report/reportActions';
import {clearSelectionTreeSearch, selectionTreeSearch} from '../../../search/searchActions';
import {OnSearch, Query} from '../../../search/searchModels';
import {AddAllToReportButton} from '../../components/AddAllToReportButton';
import {LoadingListItem} from '../../components/LoadingListItem';
import {ItemOptions, renderSelectionTreeCities} from '../../components/selection-tree-list-item/SelectionTreeListItem';
import './SelectionTreeContainer.scss';

interface StateToProps extends Query {
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
  showMetersInGraph: CallbackWithIds;
  toggleExpand: OnClickWithId;
  toggleSingleEntry: OnClickWithId;
  toggleIncludingChildren: OnClick;
  centerMapOnMeter: OnClickWithId;
  selectionTreeSearch: OnSearch;
  clearSearch: OnChange;
}

type Props = StateToProps & DispatchToProps;

const AddAllToReportButtonWrapper = withContent<Clickable & HasContent>(AddAllToReportButton);

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
      clearSearch,
      isFetching,
      showMetersInGraph,
      selectionTree,
      toggleExpand,
      openListItems,
      toggleIncludingChildren,
      toggleSingleEntry,
      itemOptions,
      centerMapOnMeter,
      selectionTreeSearch,
      primaryText,
      query,
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
    const nestedItems = cityIds.length
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

    const addAllToReport = () => showMetersInGraph(getMeterIdsWithLimit(selectionTree.entities.meters));
    const shouldShowAddAllButton: boolean = !!itemOptions.report && !isFetching && cityIds.length > 0;

    const searchBox = (
      <Column key={`search-box-${primaryText}`} className="SearchBox-Container">
        <SearchBox
          onChange={selectionTreeSearch}
          onClear={clearSearch}
          value={query}
          className="SearchBox-list SearchBox-tree"
        />
        <AddAllToReportButtonWrapper hasContent={shouldShowAddAllButton} onClick={addAllToReport}/>
      </Column>);

    const selectionTreeItems = [searchBox, ...nestedItems];

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
    search: {selectionTree: {query}},
  }: RootState): StateToProps =>
    ({
      isFetching: selectionTree.isFetching,
      selectionTree: getSelectionTree({...selectionTree, query}),
      openListItems: getOpenListItems(selectionTreeUi),
      parameters: getMeterParameters({userSelection}),
      itemOptions: {
        zoomable: isDashboardPage(routing),
        report: isReportPage(routing),
      },
      query,
      primaryText: userSelection.name,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  centerMapOnMeter,
  clearSearch: clearSelectionTreeSearch,
  addToReport,
  showMetersInGraph,
  fetchSelectionTree,
  selectionTreeSearch,
  toggleExpand: selectionTreeToggleId,
  toggleSingleEntry,
  toggleIncludingChildren,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(SelectionTreeComponent);
