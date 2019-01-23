import {TreeView, TreeViewExpandChangeEvent, TreeViewItemClickEvent} from '@progress/kendo-react-treeview';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchSelectionTree} from '../../../state/selection-tree/selectionTreeApiActions';
import {NormalizedSelectionTree} from '../../../state/selection-tree/selectionTreeModels';
import {getSelectionTreeViewItems} from '../../../state/selection-tree/selectionTreeSelectors';
import {toggleExpanded} from '../../../state/ui/selection-tree/selectionTreeActions';
import {SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {getOpenListItems} from '../../../state/ui/selection-tree/selectionTreeSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch, OnClick, OnClickWithId, uuid} from '../../../types/Types';
import {addToReport, toggleIncludingChildren, toggleSingleEntry} from '../../report/reportActions';
import {LoadingTreeViewItems} from '../components/LoadingTreeViewItems';
import {TreeViewListItem} from '../components/TreeViewItem';

const loadingStyle: React.CSSProperties = {paddingTop: 12, paddingBottom: 12};
const emptyContentTextStyle: React.CSSProperties = {
  paddingTop: 8,
  paddingBottom: 8,
  paddingLeft: 39
};

interface StateToProps {
  isFetching: boolean;
  selectionTree: NormalizedSelectionTree;
  openListItems: Set<uuid>;
  parameters: EncodedUriParameters;
  selectionTreeViewItems: SelectionTreeViewComposite[];
}

interface DispatchToProps {
  addToReport: OnClickWithId;
  fetchSelectionTree: Fetch;
  toggleExpanded: OnClickWithId;
  toggleSingleEntry: OnClickWithId;
  toggleIncludingChildren: OnClick;
}

const useForceUpdate = () => React.useState(null)[1];

type Props = StateToProps & DispatchToProps;

const TreeViewComponent = ({
  addToReport,
  fetchSelectionTree,
  isFetching,
  selectionTree,
  toggleExpanded,
  openListItems,
  toggleIncludingChildren,
  toggleSingleEntry,
  selectionTreeViewItems,
  parameters,
}: Props) => {
  React.useEffect(() => {
    fetchSelectionTree(parameters);
  }, [parameters]);
  const forceUpdate = useForceUpdate();

  const onExpandChange = (event: TreeViewExpandChangeEvent) => {
    event.item.expanded = !event.item.expanded;
    forceUpdate(null);
  };

  const onItemClick = (event: TreeViewItemClickEvent) => {
    event.item.expanded = !event.item.expanded;
    forceUpdate(null);
  };

  return selectionTreeViewItems.length
    ? (
      <TreeView
        data={selectionTreeViewItems}
        itemRender={TreeViewListItem}
        onExpandChange={onExpandChange}
        onItemClick={onItemClick}
      />
    )
    : (
      <LoadingTreeViewItems
        isFetching={isFetching}
        text={translate('no meters')}
        key="loading-list-item"
        style={loadingStyle}
        emptyContentTextStyle={emptyContentTextStyle}
      />
    );
};

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    userSelection: {userSelection},
    selectionTree,
    ui: {selectionTree: selectionTreeUi},
  }: RootState = rootState;
  return ({
    isFetching: selectionTree.isFetching,
    selectionTree,
    openListItems: getOpenListItems(selectionTreeUi),
    parameters: getMeterParameters({userSelection}),
    selectionTreeViewItems: getSelectionTreeViewItems(selectionTree),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addToReport,
  fetchSelectionTree,
  toggleExpanded,
  toggleSingleEntry,
  toggleIncludingChildren,
}, dispatch);

export const SelectionTreeContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(TreeViewComponent);
