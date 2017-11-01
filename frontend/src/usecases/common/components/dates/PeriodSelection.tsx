import {DropDownMenu, MenuItem} from 'material-ui';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {Period} from '../../../../types/Types';
import {IconCalendar} from '../icons/IconCalendar';
import {RowCenter} from '../layouts/row/Row';

interface Props {
  period: Period;
  selectPeriod: OnSelectPeriod;
}

export const PeriodSelection = (props: Props) => {
  const {period, selectPeriod} = props;

  const onSelectPeriod = (event, index, period: Period) => {
    selectPeriod(period);
  };

  return (
    <RowCenter>

      <DropDownMenu
        maxHeight={300}
        autoWidth={false}
        underlineStyle={{border: 'none'}}
        labelStyle={{height: 48, lineHeight: '48px', paddingRight: 0, paddingLeft: 24, fontSize: 14}}
        iconStyle={{fill: 'black', height: 48, width: 48, right: 0, top: 0, padding: 0}}
        style={{width: 165}}
        className="PeriodSelection"
        value={period}
        onChange={onSelectPeriod}
        iconButton={<IconCalendar/>}
      >
        <MenuItem value={Period.now} label={'22 nov'} primaryText={translate('one day')}/>
        <MenuItem value={Period.week} label={'15 nov - 22 nov'} primaryText={translate('last week')}/>
        <MenuItem value={Period.month} label={'22 okt - 22 nov'} primaryText={translate('last month')}/>
        <MenuItem value={Period.quarter} label={'22 aug - 22 nov'} primaryText={translate('last quarter')}/>
      </DropDownMenu>
    </RowCenter>
  );
};
