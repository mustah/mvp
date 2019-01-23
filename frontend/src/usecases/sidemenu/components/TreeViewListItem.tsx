import {ItemRenderProps} from '@progress/kendo-react-treeview';
import * as React from 'react';
import {menuItemStyle} from '../../../app/themes';
import {FirstUpper} from '../../../components/texts/Texts';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {TreeViewListItemMeterContainer} from '../containers/TreeViewListItemMeterContainer';

const treeViewItemStyle: React.CSSProperties = {
  paddingLeft: 8,
  paddingTop: 6,
  paddingBottom: 6,
  cursor: 'pointer',
  ...menuItemStyle.textStyle,
};

export const TreeViewListItem = ({item}: ItemRenderProps) => {
  const props = item as SelectionTreeViewComposite;

  return props.type === SelectionTreeItemType.meter
    ? <TreeViewListItemMeterContainer {...props}/>
    : <FirstUpper style={treeViewItemStyle}>{props.text}</FirstUpper>;
};
