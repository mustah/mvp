import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';
import {OnChange} from '../../types/Types';

interface Props {
  onChange: OnChange;
  clear?: boolean;
}

interface State {
  value: string;
}

const searchStyle: React.CSSProperties = {
  position: 'absolute',
  right: 12,
  top: 7,
  color: '#7b7b7b',
};

export class SearchBox extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {value: ''};
  }

  render() {
    const {clear} = this.props;
    const {value} = this.state;

    return (
      <div className="SearchBox">
        <input
          autoFocus={true}
          type="textfield"
          className="SearchBox-input"
          value={clear ? '' : value}
          onChange={this.onChange}
        />
        <ActionSearch style={searchStyle}/>
      </div>
    );
  }

  onChange = (event: any) => {
    event.preventDefault();
    const value = event.target.value;
    this.setState({value});
    this.props.onChange(value);
  }
}
