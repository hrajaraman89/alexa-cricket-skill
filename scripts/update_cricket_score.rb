require 'open-uri'
require 'json'
require 'aws-sdk'

DB_CLIENT = Aws::DynamoDB::Client.new(region: 'us-east-1',
                                      credentials: Aws::InstanceProfileCredentials.new())

#override the | for the empty String because Dynamodb does not allow empty string
class String
  def |(what)
    self.empty? ? what : self
  end
end

def update_cricket_ids(cricket_summary)
  cricket_ids = []
  modules = cricket_summary['modules']
  modules.each { |_, arr|
    arr.each { |match|
      if match['category'] == 'intl'
        cricket_ids += match['matches']
      end
    }
  }

  ids_as_string = cricket_ids.uniq

  item = {
      'id' => 'intl',
      'gameIds' => ids_as_string.map(&:to_i),
      'lastUpdated' => Time.now.utc.to_i
  }

  ids = {
      table_name: 'CricketGameIds',
      item: item
  }

  DB_CLIENT.put_item(ids)

  ids_as_string
end

def update_cricket_database()
  cricket_summary = JSON.parse open('http://www.espncricinfo.com/netstorage/summary.json').read

  ids = update_cricket_ids(cricket_summary)

  puts ids

  cricket_matches = cricket_summary['matches']

  ids.each { |i|
    match = cricket_matches[i]

    url = match['url']
    game_id = url.match('([0-9]+).html').captures[0]

    cricket_game = JSON.parse open("http://www.espncricinfo.com/ci/engine/match/#{game_id}.json").read
    cricket_match = cricket_game['match']
    cricket_innings = cricket_game['innings']

    current_innings = cricket_innings[0]

    cricket_innings.each { |innings|
      if innings['live_current'] == '1'
        current_innings = innings
        break
      end
    }

    winnerId = (cricket_match['winner_team_id'] || '0').to_i
    jsonStatus = cricket_match['match_status'].upcase || 'DORMANT'
    realStatus = winnerId == 0 ? jsonStatus : 'COMPLETE'

    item = {
        'id' => i.to_i,
        'externalId' => game_id.to_i,
        'teamAName' => cricket_match['team1_name'],
        'teamAId' => cricket_match['team1_country_id'].to_i,
        'teamBName' => cricket_match['team2_name'],
        'teamBId' => cricket_match['team2_country_id'].to_i,
        'venue' => cricket_match['ground_name'],
        'shortVenue' => cricket_match['ground_small_name'],
        'status' => realStatus,
        'liveStatus' => cricket_game['live']['status'],
        'winnerId' => winnerId,
        'lastUpdated' => Time.now.utc.to_i,
        'battingTeamId' => current_innings['batting_team_id'].to_i,
        'bowlingTeamId' => current_innings['bowling_team_id'].to_i,
        'runs' => current_innings['runs'].to_i,
        'runRate' => (current_innings['run_rate'] || '0.0').to_f,
        'wickets' => (current_innings['wickets'] || '0').to_i,
        'target' => (current_innings['target'] || '0').to_i,
        'overs' => current_innings['overs']

    }

    row = {
        table_name: 'CricketGameDetail',
        item: item
    }

    puts row

    resp = DB_CLIENT.put_item(row)

    puts resp

    sleep 2
  }
end


while true do
  update_cricket_database
  puts 'sleeping for 30...\n'
  sleep 30
end