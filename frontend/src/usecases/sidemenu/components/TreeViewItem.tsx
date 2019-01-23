import {ItemRenderProps} from '@progress/kendo-react-treeview';
import * as React from 'react';
import {menuItemStyle} from '../../../app/themes';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {FirstUpper} from '../../../components/texts/Texts';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {Styled, uuid} from '../../../types/Types';

const labelStyle: React.CSSProperties = {
  marginLeft: 4,
  paddingLeft: 0,
  paddingRight: 0,
  cursor: 'normal',
  width: 126,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
};

const iconStyle: React.CSSProperties = {
  marginLeft: 0,
  marginRight: 4,
  padding: 0,
  width: 24,
  height: 24,
};

const treeViewItemStyle: React.CSSProperties = {
  paddingLeft: 8,
  paddingTop: 6,
  paddingBottom: 6,
  ...menuItemStyle.textStyle,
};

const treeViewMeterStyle: React.CSSProperties = {
  ...menuItemStyle.textStyle,
};

const TreeViewItemMeter = ({text, id, style}: SelectionTreeViewComposite & Styled) => {
  const addMeterToReport = () => console.log('addMeterToReport'); // addToReport(item.id);
  const selectedId: Maybe<uuid> = Maybe.maybe(id);

  return (
    <OpenDialogInfoButton
      label={text}
      autoScrollBodyContent={true}
      iconStyle={iconStyle}
      labelStyle={labelStyle}
      onLabelClick={addMeterToReport}
      style={style}
    >
      <MeterDetailsContainer selectedId={selectedId}/>
    </OpenDialogInfoButton>
  );
};

export const TreeViewListItem = ({item}: ItemRenderProps) => {
  const element: SelectionTreeViewComposite = item as SelectionTreeViewComposite;

  return element.type === SelectionTreeItemType.meter
    ? <TreeViewItemMeter {...element} style={treeViewMeterStyle}/>
    : <FirstUpper style={treeViewItemStyle}>{element.text}</FirstUpper>;
};
