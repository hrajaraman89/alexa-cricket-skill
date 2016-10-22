require 'open-uri'
require 'json'
require 'aws-sdk'

DB_CLIENT = Aws::DynamoDB::Client.new(
  region: 'us-east-1',
  credentials: Aws::InstanceProfileCredentials.new()
)

#override the | for the empty String because Dynamodb does not allow empty string
class String
  def |(what)
    self.empty? ? what : self
  end
end

def update_cricket_ids(cricket_summary)
   cricket_ids = []
   modules = cricket_summary['modules']
   modules.each{ |key, arr|
     arr.each{|match|
      if match['category'] == 'intl'
        cricket_ids += match['matches']
      end
     }
   }

  item = {"id" => 'intl', 'cricket_ids' => cricket_ids.uniq}

  DB_CLIENT.put_item({
   table_name: 'CricketGameIds',
   item: item
  })
end

def update_cricket_database()
  cricket_summary = JSON.parse open("http://www.espncricinfo.com/netstorage/summary.json").read
  
  update_cricket_ids(cricket_summary) 
  
  cricket_matches = cricket_summary["matches"]

  cricket_matches.each { |id, match|
    url = match["url"]
    game_id = url.match('([0-9]+).html').captures[0]
  
    cricket_game = JSON.parse open("http://www.espncricinfo.com/ci/engine/match/#{game_id}.json").read
    cricket_match = cricket_game['match']

    item = {
      "id" => id.to_i,
      "external_id" => game_id.to_i, 
      "teamAName" => cricket_match['team1_name'],
      'teamAId' => cricket_match['team1_country_id'],
      "teamBName" => cricket_match['team2_name'], 
      'teamBId' => cricket_match['team2_country_id'],
      "venue" => cricket_match["ground_name"],
      "shortVenue" => cricket_match["ground_small_name"],
      "status"=> cricket_match["match_status"],
      "liveStatus" => cricket_game['live']['status'].capitalize  | 'UNKNOWN',
      "winnerId"=> cricket_match['winner_team_id'],
      "lastUpdated" => Time.now.utc.to_i,
    }

    resp = DB_CLIENT.put_item({
      table_name: "CricketGameDetail",
      item: item
    })  
  } 
end


while true do
  update_cricket_database
  sleep 30
end
