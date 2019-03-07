import * as React from 'react';
import {menuItemStyle} from '../../../app/themes';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {SelectionTreeViewComposite} from '../../../state/ui/selection-tree/selectionTreeModels';
import {uuid} from '../../../types/Types';

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

const treeViewMeterStyle: React.CSSProperties = {
  ...menuItemStyle.textStyle,
  cursor: 'pointer',
};

export const TreeViewListItemMeter = ({text, id}: SelectionTreeViewComposite) => {
  const selectedId = Maybe.maybe<uuid>(id);
  return (
    <OpenDialogInfoButton
      label={text}
      autoScrollBodyContent={true}
      iconStyle={iconStyle}
      labelStyle={labelStyle}
      style={treeViewMeterStyle}
      title={firstUpperTranslated('add to report')}
    >
      <MeterDetailsContainer selectedId={selectedId} useCollectionPeriod={false}/>
    </OpenDialogInfoButton>
  );
};
