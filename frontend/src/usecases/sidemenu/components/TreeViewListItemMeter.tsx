import * as React from 'react';
import {routes} from '../../../app/routes';
import {menuItemStyle} from '../../../app/themes';
import {OpenDialogInfoButton} from '../../../components/dialog/OpenDialogInfoButton';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {Maybe} from '../../../helpers/Maybe';
import {history} from '../../../index';
import {firstUpperTranslated} from '../../../services/translationService';
import {uuid} from '../../../types/Types';
import {TreeViewMeterListItemProps} from '../containers/TreeViewListItemMeterContainer';

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

export const TreeViewListItemMeter = ({text, id, addToReport}: TreeViewMeterListItemProps) => {
  const addMeterToReport = () => {
    addToReport(id);
    history.push(`${routes.report}/${id}`);
  };
  const selectedId: Maybe<uuid> = Maybe.maybe(id);

  return (
    <OpenDialogInfoButton
      label={text}
      autoScrollBodyContent={true}
      iconStyle={iconStyle}
      labelStyle={labelStyle}
      onLabelClick={addMeterToReport}
      style={treeViewMeterStyle}
      title={firstUpperTranslated('add to report')}
    >
      <MeterDetailsContainer selectedId={selectedId}/>
    </OpenDialogInfoButton>
  );
};
