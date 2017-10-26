import {FlatButton} from 'material-ui';
import Dialog from 'material-ui/Dialog';
import 'MeteringPoint.scss';
import * as React from 'react';
import {translate} from '../../../../../services/translationService';
import {Column} from '../../layouts/column/Column';
import {Row} from '../../layouts/row/Row';
import {StatusIcon} from '../status/StatusIcon';

interface MeteringPointProps {
  id: string;
}

interface MeteringPointState {
  displayDialog: boolean;
}

export class MeteringPoint extends React.Component<MeteringPointProps, MeteringPointState> {

  constructor(props) {
    super(props);

    this.state = {
      displayDialog: false,
    };
  }

  render() {
    const {id} = this.props;

    const open = (event: any): void => {
      event.preventDefault();
      this.setState((current) => ({...current, displayDialog: true}));
    };

    const close = (): void =>
      this.setState((current) => ({...current, displayDialog: false}));

    const actions = [
      (
        <FlatButton
          label={translate('close')}
          primary={true}
          onClick={close}
        />
      ),
    ];

    // TODO extract the Dialog into its own component, and keep track of its open/close state in the root.ui reducer
    return (
      <div>
        <a href={'/#/meter/' + id} onClick={open}>{id}</a>
        <Dialog
          actions={actions}
          open={this.state.displayDialog}
          onRequestClose={close}
        >
          <h2 className="capitalize">{translate('meter details')}</h2>
          <Row>
            <Column>
              <img className="MeterGraphics" src={'cme2100.jpg'}/>
            </Column>
            <Column>
              <Row>
                <Column>
                  <Row>
                    {translate('meter id')}
                  </Row>
                  <Row>
                    12000747
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('product model')}
                  </Row>
                  <Row>
                    KAM
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('city')}
                  </Row>
                  <Row>
                    Perstorp
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('address')}
                  </Row>
                  <Row>
                    Duvstigen 5
                  </Row>
                </Column>
              </Row>
              <Row>
                <Column>
                  <Row>
                    {translate('collection')}
                  </Row>
                  <Row>
                    <StatusIcon code={0}/>
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('validation')}
                  </Row>
                  <Row>
                    <StatusIcon code={3}/>
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('status')}
                  </Row>
                  <Row>
                    Läckage
                  </Row>
                </Column>
                <Column>
                  <Row>
                    {translate('status')}
                  </Row>
                  <Row>
                    {translate('action pending')}
                  </Row>
                </Column>
              </Row>
            </Column>
          </Row>
        </Dialog>
      </div>
    );
  }
}
