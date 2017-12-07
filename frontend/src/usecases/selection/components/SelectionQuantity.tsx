import {Dialog} from 'material-ui';
import DropDownMenu from 'material-ui/DropDownMenu';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import MenuItem from 'material-ui/MenuItem';
import AlertAddAlert from 'material-ui/svg-icons/alert/add-alert';
import TextField from 'material-ui/TextField';
import * as React from 'react';
import {colors, floatingLabelFocusStyle, underlineFocusStyle} from '../../../app/themes';
import {ColumnCenter} from '../../../components/layouts/column/Column';
import {RowBottom} from '../../../components/layouts/row/Row';
import {Clickable} from '../../../types/Types';

const textFieldStyle: React.CSSProperties = {
  marginBottom: -8, /* TODO another instance of "wasted too much time to find the hidden inlined style". So sorry */
  marginLeft: 16,
  fontSize: 14,
  width: 180,
};

const style = {
  padding: 2,
  width: 35,
  height: 35,
};

const IconInput = (props: Clickable) => (
  <IconButton
    className="IconButton"
    style={style}
    onClick={props.onClick}
  >
    <AlertAddAlert
      color={colors.blue}
    />
  </IconButton>
);

interface State {
  showDiffTempDialog: boolean;
  chosenQuantity: string;
  chosenDirection: string;
}

export class SelectionQuantity extends React.Component<{}, State> {

  constructor(props) {
    super(props);
    this.state = {
      showDiffTempDialog: false,
      chosenQuantity: 'Heat, return temp',
      chosenDirection: '>=',
    };
  }

  showDiffStuff = () => this.setState({showDiffTempDialog: true});

  hideDiffTempDialog = () => this.setState({showDiffTempDialog: false});

  selectQuantity = (event, index: number, chosenQuantity: string) => this.setState({chosenQuantity});

  selectDirection = (event, index: number, chosenDirection: string) => this.setState({chosenDirection});

  render() {
    const {showDiffTempDialog, chosenDirection, chosenQuantity} = this.state;

    const quantities = [
      'Energi',
      'Volym',
      'Effekt',
      'Flöde',
      'Flödestemperatur',
      'Returtemperatur',
      'Temperaturskillnad',
    ].map((quantity: string) => (
      <MenuItem
        key={quantity}
        label={quantity}
        primaryText={quantity}
        value={quantity}
      />
    ));

    const quantityActions = [
      (
        <FlatButton
          primary={true}
          onClick={this.hideDiffTempDialog}
          key={'Spara'}
          label={'Spara'}
        />
      ),
      (
        <FlatButton
          onClick={this.hideDiffTempDialog}
          key={'Stäng'}
          label={'Stäng'}
        />
      ),
    ];

    return (
      <div>
        <IconInput
          onClick={this.showDiffStuff}
        />
        <Dialog
          actions={quantityActions}
          open={showDiffTempDialog}
          onRequestClose={this.hideDiffTempDialog}
        >
          <h2>Filtrera på storhet</h2>
          <ColumnCenter>
            <RowBottom>
              <DropDownMenu
                maxHeight={300}
                onChange={this.selectQuantity}
                value={chosenQuantity}
              >
                <MenuItem
                  key={'Heat, return temp'}
                  label={'Heat, return temp'}
                  primaryText={'Heat, return temp'}
                  style={{fontWeight: 'bold'}}
                  value={'Heat, return temp'}
                />
                {quantities}
              </DropDownMenu>
              <DropDownMenu
                onChange={this.selectDirection}
                value={chosenDirection}
              >
                <MenuItem key={'>='} primaryText={'>='} label={'>='} value={'>='}/>
                <MenuItem key={'>'} primaryText={'>'} label={'>'} value={'>'}/>
                <MenuItem key={'<='} primaryText={'<='} label={'<='} value={'<='}/>
                <MenuItem key={'<'} primaryText={'<'} label={'<'} value={'<'}/>
              </DropDownMenu>
              <TextField
                style={textFieldStyle}
                floatingLabelFocusStyle={floatingLabelFocusStyle}
                hintText="Skriv ett värde här"
                underlineFocusStyle={underlineFocusStyle}
              />
            </RowBottom>
          </ColumnCenter>
        </Dialog>
      </div>
    );
  }
}
