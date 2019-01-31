import {ItemRenderProps} from '@progress/kendo-react-treeview';
import * as React from 'react';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {TreeViewListItemGroupContainer} from '../containers/TreeViewListItemGroupContainer';
import {TreeViewListItemMeterContainer} from '../containers/TreeViewListItemMeterContainer';

export const TreeViewListItem = ({item}: ItemRenderProps) => {
  const props = item as SelectionTreeViewComposite;

  return props.type === SelectionTreeItemType.meter
    ? <TreeViewListItemMeterContainer {...props}/>
    : <TreeViewListItemGroupContainer {...props}/>;
};
