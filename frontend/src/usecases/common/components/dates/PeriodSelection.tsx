import {DropDownMenu, MenuItem} from 'material-ui';
import DatePicker from 'material-ui/DatePicker';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import 'PeriodSelection.scss';
import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {OnSelectPeriod} from '../../../../state/search/selection/selectionModels';
import {Period} from '../../../../types/Types';
import {colors, fontSizeNormal, listItemStyle} from '../../../app/themes';
import {IconCalendar} from '../icons/IconCalendar';
import {Row} from '../layouts/row/Row';

const height = 32;

const style: React.CSSProperties = {
  height,
  width: 158,
  fontSize: fontSizeNormal,
  border: `1px solid ${colors.borderColor}`,
  borderRadius: 3,
  marginLeft: 24,
  marginBottom: 16,
};

const underlineStyle: React.CSSProperties = {
  border: 'none',
};

const labelStyle: React.CSSProperties = {
  height,
  lineHeight: 1,
  paddingRight: 0,
  paddingLeft: 16,
  fontSize: 14,
  display: 'flex',
  alignItems: 'center',
};

const iconStyle: React.CSSProperties = {
  fill: colors.lightBlack,
  height,
  width: 46,
  right: 0,
  top: 0,
  padding: 0,
};

interface Props {
  period: Period;
  selectPeriod: OnSelectPeriod;
}

interface State {
  timePickerVisible: boolean;
}

export class PeriodSelection extends React.Component<Props, State> {

  constructor() {
    super();
    this.state = {
      timePickerVisible: false,
    };
  }

  hideCustomPicker = () => {
    this.setState({timePickerVisible: false});
  }

  showCustomPicker = () => {
    this.setState({timePickerVisible: true});
  }

  render() {
    const {period, selectPeriod} = this.props;
    const {timePickerVisible} = this.state;

    const onSelectPeriod = (event, index, period: Period) => {
      selectPeriod(period);
    };

    const timePeriods = [
      {
        value: Period.latest,
        chosen: 'Senaste', // TODO demo purposes only. I imagine we will input real timestamps here in the future,
                           // anyways
        alternative: 'Senaste',
      },
      {
        value: Period.currentMonth,
        chosen: '1 nov - 22 nov',
        alternative: translate('current month'),
      },
      {
        value: Period.previousMonth,
        chosen: '1 okt - 31 okt',
        alternative: translate('previous month'),
      },
      {
        value: Period.currentWeek,
        chosen: '20 nov - 22 nov',
        alternative: translate('current week'),
      },
      {
        value: Period.previous7Days,
        chosen: '16 nov - 22 nov',
        alternative: translate('last 7 days'),
      },
    ];

    const timePeriodComponents = timePeriods.map((tp) => (
      <MenuItem
        className="TimePeriod"
        key={tp.alternative}
        label={tp.chosen}
        primaryText={tp.alternative}
        style={listItemStyle}
        value={tp.value}
      />
    ));

    timePeriodComponents.push(
      (
        <MenuItem
          className="TimePeriod"
          key={translate('pick a date')}
          label={'1 okt - 31 okt'}
          onClick={this.showCustomPicker}
          primaryText={translate('pick a date')}
          style={listItemStyle}
          value={Period.custom}
        />
      ),
    );

    const actions = [
      (
        <FlatButton
          key={'Stäng'}
          label={'Stäng'}
          onClick={this.hideCustomPicker}
        />
      ),
    ];

    const customPickerDialog = (
      <Dialog
        actions={actions}
        onRequestClose={this.hideCustomPicker}
        open={true}
      >
        <h2>Välj datum</h2>
        <p>Mellan</p>
        <DatePicker
          autoOk={true}
          hintText={'Startdatum'}
        />
        <p>och</p>
        <DatePicker
          autoOk={true}
          hintText={'Slutdatum'}
        />
      </Dialog>
    );

    const customPicker = timePickerVisible ? customPickerDialog : null;

    return (
      <Row className="PeriodSelection">
        {customPicker}
        <DropDownMenu
          className="PeriodSelection-dropdown"
          maxHeight={300}
          underlineStyle={underlineStyle}
          labelStyle={labelStyle}
          iconStyle={iconStyle}
          style={style}
          value={period}
          onChange={onSelectPeriod}
          iconButton={<IconCalendar className="IconCalendar"/>}
          selectedMenuItemStyle={{color: colors.blue}}
        >
          {timePeriodComponents}
        </DropDownMenu>
      </Row>
    );
  }
}
