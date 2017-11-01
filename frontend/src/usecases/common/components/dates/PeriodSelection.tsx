import {DropDownMenu, MenuItem} from 'material-ui';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {periods} from '../../../../types/constants';
import {IdNamed, Period} from '../../../../types/Types';
import {IconCalendar} from '../icons/IconCalendar';
import {RowCenter} from '../layouts/row/Row';

interface State {
  selectedPeriod: IdNamed;
}

export class PeriodSelection extends React.Component<{}, State> {

  constructor(props) {
    super(props);
    this.state = {selectedPeriod: periods[0]};
  }

  render() {
    const dropdownChanged = (event, index) => {
      this.setState({selectedPeriod: periods[index]});
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
          value={this.state.selectedPeriod.id}
          onChange={dropdownChanged}
          iconButton={<IconCalendar/>}
        >
          <MenuItem value={Period.now} label={'22 nov'} primaryText={translate('one day')}/>
          <MenuItem value={Period.week} label={'15 nov - 22 nov'} primaryText={translate('last week')}/>
          <MenuItem value={Period.month} label={'22 okt - 22 nov'} primaryText={translate('last month')}/>
          <MenuItem value={Period.quarter} label={'22 aug - 22 nov'} primaryText={translate('last quarter')}/>
        </DropDownMenu>
      </RowCenter>
    );
  }
}
