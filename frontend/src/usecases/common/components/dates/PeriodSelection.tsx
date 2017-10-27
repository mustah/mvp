import * as React from 'react';
import {Separator} from '../../../dashboard/components/separators/Separator';
import {IconCalendar} from '../icons/IconCalendar';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import 'PeriodSelection.scss';
import {DropDownMenu, MenuItem} from 'material-ui';
import {IdNamed, uuid} from '../../../../types/Types';

interface Props {
  selectedPeriod?: uuid;
  periods?: IdNamed[];
}

interface State {
  selectedPeriod: IdNamed;
  periods: IdNamed[];
}

export class PeriodSelection extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {
      selectedPeriod: {id: 0, name: 'Week'},
      periods: [{id: 0, name: 'Week'}, {id: 1, name: 'Month'}, {id: 2, name: 'Quarter'}, {id: 3, name: 'Year'}],
    };
  }

  render() {
    const dropdownChanged = (event, index, value) => {
      this.setState({selectedPeriod: this.state.periods[index]});
    };

    return (
      <Column>
        <Normal className="uppercase">Period</Normal>
        <Separator/>
        <Row className="Row-center">
          <IconCalendar/>
          <DropDownMenu
            maxHeight={300}
            autoWidth={false}
            className="periodSelection"
            value={this.state.selectedPeriod.id}
            onChange={dropdownChanged}
          >
            <MenuItem value={0} label={'9 nov - 16 nov'} primaryText="Week"/>
            <MenuItem value={1} label={'16 okt - 16 nov'} primaryText="Month"/>
            <MenuItem value={2} label={'16 aug - 16 nov'} primaryText="Quarter"/>
            <MenuItem value={3} label={'2016-11-16 - 2017-11-16'} primaryText="Year"/>
          </DropDownMenu>
        </Row>
      </Column>
    );
  }
}
