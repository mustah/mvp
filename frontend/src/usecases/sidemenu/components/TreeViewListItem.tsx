import {ItemRenderProps} from '@progress/kendo-react-treeview';
import * as React from 'react';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {TreeViewListItemMeter} from './TreeViewListItemMeter';

export const TreeViewListItem = ({item}: ItemRenderProps) => {
  const props = item as SelectionTreeViewComposite;

  return props.type === SelectionTreeItemType.meter
    ? <TreeViewListItemMeter {...props}/>
    : null;
};
